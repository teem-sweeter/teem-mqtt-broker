package com.jjc.mqtt.monitor;

import io.moquette.broker.ClientDescriptor;
import io.moquette.broker.Server;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Service
@EnableScheduling
public class MonitorService implements ApplicationEventPublisherAware {

    private static final Logger log = LoggerFactory.getLogger(MonitorService.class);

    private final Server mqttBroker;

    private final Map<String, ClientInfo> clients = new ConcurrentHashMap<>();
    private final Map<String, Long> topicMessageCounts = new ConcurrentHashMap<>();
    private final LinkedBlockingDeque<MqttMessageRecord> recentMessages = new LinkedBlockingDeque<>(10);

    private final AtomicLong totalConnections = new AtomicLong(0);
    private final AtomicLong totalDisconnections = new AtomicLong(0);
    private final AtomicLong totalMessagesSent = new AtomicLong(0);
    private final AtomicLong totalMessagesReceived = new AtomicLong(0);
    private final AtomicLong messageCountInLastSecond = new AtomicLong(0);
    private volatile double currentMessageRate = 0.0;

    private final ReadWriteLock statsLock = new ReentrantReadWriteLock();
    private long startTime;
    private ApplicationEventPublisher eventPublisher;

    public MonitorService(Server mqttBroker) {
        this.mqttBroker = mqttBroker;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @PostConstruct
    public void init() {
        startTime = System.currentTimeMillis();
        log.info("MonitorService 初始化完成");
    }

    @PreDestroy
    public void destroy() {
        log.info("MonitorService 关闭，清理资源");
    }

    /**
     * 客户端连接（完整信息）
     */
    public void onClientConnect(String clientId, String username, int keepAlive,
                                String protocolVersion, boolean cleanSession,
                                boolean willFlag, String willTopic, String willMessage,
                                int willQos, boolean willRetain) {
        ClientInfo clientInfo = ClientInfo.builder()
                .clientId(clientId)
                .username(username)
                .keepAlive(keepAlive)
                .connectTime(System.currentTimeMillis())
                .subscriptions(new CopyOnWriteArraySet<>())
                .subscriptionDetails(new CopyOnWriteArraySet<>())
                .protocolVersion(protocolVersion)
                .cleanSession(cleanSession)
                .willFlag(willFlag)
                .willTopic(willTopic)
                .willMessage(willMessage)
                .willQos(willQos)
                .willRetain(willRetain)
                .build();

        ClientInfo previous = clients.put(clientId, clientInfo);
        if (previous != null) {
            log.warn("检测到重复 ClientID 连接: clientId={}, 旧连接时间={}, 新连接时间={}",
                    clientId, previous.getConnectTime(), clientInfo.getConnectTime());
        }
        totalConnections.incrementAndGet();
        publishEvent(new MonitorEvent("CLIENT_CONNECTED", clientInfo));
    }

    public void onClientDisconnect(String clientId) {
        ClientInfo removed = clients.remove(clientId);
        if (removed != null) {
            totalDisconnections.incrementAndGet();
            publishEvent(new MonitorEvent("CLIENT_DISCONNECTED", removed));
        }
    }

    public void onMessagePublish(String topic, String payload, int qos, String clientId) {
        MqttMessageRecord record = MqttMessageRecord.builder()
                .topic(topic)
                .payload(payload)
                .qos(qos)
                .clientId(clientId)
                .timestamp(System.currentTimeMillis())
                .direction("INBOUND")
                .build();

        addMessageRecord(record);
        topicMessageCounts.merge(topic, 1L, Long::sum);
        totalMessagesReceived.incrementAndGet();
        messageCountInLastSecond.incrementAndGet();
        publishEvent(new MonitorEvent("MESSAGE_RECEIVED", record));
    }

    /**
     * 客户端订阅（含订阅详情）
     */
    public void onTopicSubscribe(String clientId, SubscriptionInfo subInfo) {
        ClientInfo client = clients.get(clientId);
        if (client != null) {
            client.getSubscriptions().add(subInfo.getTopic());
            client.getSubscriptionDetails().add(subInfo);
            publishEvent(new MonitorEvent("TOPIC_SUBSCRIBED",
                    new TopicSubscription(clientId, subInfo.getTopic(), subInfo.getQos())));
        }
    }

    public void onTopicUnsubscribe(String clientId, String topic) {
        ClientInfo client = clients.get(clientId);
        if (client != null) {
            client.getSubscriptions().remove(topic);
            client.getSubscriptionDetails().removeIf(s -> topic.equals(s.getTopic()));
            publishEvent(new MonitorEvent("TOPIC_UNSUBSCRIBED",
                    new TopicSubscription(clientId, topic, 0)));
        }
    }

    /**
     * 刷新客户端的 IP/端口 和 Session 运行指标
     */
    public void refreshClientMetrics(String clientId) {
        // IP/端口
        Collection<ClientDescriptor> descriptors = mqttBroker.listConnectedClients();
        Map<String, ClientDescriptor> descMap = descriptors.stream()
                .collect(Collectors.toMap(ClientDescriptor::getClientID, v -> v, (a, b) -> b));
        ClientDescriptor desc = descMap.get(clientId);
        ClientInfo clientInfo = clients.get(clientId);
        if (desc != null && clientInfo != null) {
            clientInfo.setIpAddress(desc.getAddress());
            clientInfo.setPort(desc.getPort());
        }

        // Session 运行指标（通过反射获取，兼容 Moquette 0.18.0）
        if (clientInfo != null) {
            try {
                Object session = getSessionFromRegistry(clientId);
                if (session != null) {
                    Class<?> sessionClass = session.getClass();

                    // inflight 窗口大小
                    Field inflightField = sessionClass.getDeclaredField("inflightWindow");
                    inflightField.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    Map<Integer, ?> inflight = (Map<Integer, ?>) inflightField.get(session);
                    clientInfo.setInflightCount(inflight != null ? inflight.size() : 0);

                    // 队列积压
                    Field queueField = sessionClass.getDeclaredField("sessionQueue");
                    queueField.setAccessible(true);
                    Object queue = queueField.get(session);
                    if (queue != null) {
                        Field sizeField = queue.getClass().getDeclaredField("queue");
                        sizeField.setAccessible(true);
                        Object jdkQueue = sizeField.get(queue);
                        if (jdkQueue instanceof java.util.Queue<?> q) {
                            clientInfo.setQueuedCount(q.size());
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("获取 Session 指标失败: clientId={}", clientId);
            }
        }
    }

    private Object getSessionFromRegistry(String clientId) {
        try {
            Field sessionsField = Server.class.getDeclaredField("sessions");
            sessionsField.setAccessible(true);
            Object sessionRegistry = sessionsField.get(mqttBroker);

            Field poolField = sessionRegistry.getClass().getDeclaredField("pool");
            poolField.setAccessible(true);
            @SuppressWarnings("unchecked")
            ConcurrentMap<String, Object> pool = (ConcurrentMap<String, Object>) poolField.get(sessionRegistry);
            return pool.get(clientId);
        } catch (Exception e) {
            return null;
        }
    }

    private void addMessageRecord(MqttMessageRecord record) {
        if (!recentMessages.offer(record)) {
            recentMessages.pollFirst();
            recentMessages.offer(record);
        }
    }

    @Scheduled(fixedRate = 1000)
    public void calculateMessageRate() {
        long count = messageCountInLastSecond.getAndSet(0);
        currentMessageRate = count;
    }

    public MqttStats getStats() {
        statsLock.readLock().lock();
        try {
            long uptime = System.currentTimeMillis() - startTime;
            return MqttStats.builder()
                    .totalConnections(totalConnections.get())
                    .totalDisconnections(totalDisconnections.get())
                    .totalMessagesSent(totalMessagesSent.get())
                    .totalMessagesReceived(totalMessagesReceived.get())
                    .currentConnections(clients.size())
                    .totalSubscriptions(clients.values().stream()
                            .mapToInt(c -> c.getSubscriptions().size())
                            .sum())
                    .totalTopics(topicMessageCounts.size())
                    .messagesPerSecond(currentMessageRate)
                    .uptime(uptime)
                    .build();
        } finally {
            statsLock.readLock().unlock();
        }
    }

    public List<ClientInfo> getConnectedClients() {
        return new ArrayList<>(clients.values());
    }

    public Optional<ClientInfo> getClient(String clientId) {
        return Optional.ofNullable(clients.get(clientId));
    }

    public void refreshClientIpPort(String clientId) {
        Collection<ClientDescriptor> clientDescriptors = mqttBroker.listConnectedClients();
        Map<String, ClientDescriptor> clientDescriptorMap = clientDescriptors.stream()
                .collect(Collectors.toMap(ClientDescriptor::getClientID, v -> v, (a, b) -> b));
        ClientDescriptor clientDescriptor = clientDescriptorMap.get(clientId);
        if (clientDescriptor != null) {
            ClientInfo clientInfo = clients.get(clientId);
            if (clientInfo != null) {
                clientInfo.setIpAddress(clientDescriptor.getAddress());
                clientInfo.setPort(clientDescriptor.getPort());
            }
        }
    }

    public List<MqttMessageRecord> getRecentMessages(int limit) {
        List<MqttMessageRecord> result = new ArrayList<>();
        int count = 0;
        Iterator<MqttMessageRecord> it = recentMessages.descendingIterator();
        while (it.hasNext() && count < limit) {
            result.add(it.next());
            count++;
        }
        Collections.reverse(result);
        return result;
    }

    public Map<String, Long> getTopicStats() {
        return new HashMap<>(topicMessageCounts);
    }

    public boolean disconnectClient(String clientId) {
        ClientInfo client = clients.get(clientId);
        if (client != null) {
            boolean disconnected = mqttBroker.disconnectClient(clientId);
            publishEvent(new MonitorEvent("FORCE_DISCONNECT", client));
            log.info("强制断开客户端: ClientID={}, 结果={}", clientId, disconnected);
            return true;
        }
        return false;
    }

    public void clearStats() {
        statsLock.writeLock().lock();
        try {
            totalConnections.set(0);
            totalDisconnections.set(0);
            totalMessagesSent.set(0);
            totalMessagesReceived.set(0);
            topicMessageCounts.clear();
            recentMessages.clear();
        } finally {
            statsLock.writeLock().unlock();
        }
    }

    private void publishEvent(MonitorEvent event) {
        if (eventPublisher != null) {
            eventPublisher.publishEvent(event);
        }
    }

    public static class MonitorEvent {
        private final String type;
        private final Object data;
        private final long timestamp;

        public MonitorEvent(String type, Object data) {
            this.type = type;
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }

        public String getType() { return type; }
        public Object getData() { return data; }
        public long getTimestamp() { return timestamp; }
    }

    public static class TopicSubscription {
        private final String clientId;
        private final String topic;
        private final int qos;

        public TopicSubscription(String clientId, String topic, int qos) {
            this.clientId = clientId;
            this.topic = topic;
            this.qos = qos;
        }

        public String getClientId() { return clientId; }
        public String getTopic() { return topic; }
        public int getQos() { return qos; }
    }
}
