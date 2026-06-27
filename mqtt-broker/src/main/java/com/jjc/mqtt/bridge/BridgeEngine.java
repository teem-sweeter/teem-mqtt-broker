package com.jjc.mqtt.bridge;

import com.jjc.mqtt.bridge.route.RouteRule;
import com.jjc.mqtt.bridge.route.TopicMatcher;
import io.moquette.broker.Server;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttPublishVariableHeader;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.eclipse.paho.mqttv5.client.*;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 桥接路由引擎
 * <p>
 * 管理单条桥接链路的生命周期：Paho 客户端连接、出向转发、入向注入。
 * 每条桥接链路对应一个 BridgeEngine 实例。
 *
 * @author sweeter
 */
public class BridgeEngine {

    private static final Logger log = LoggerFactory.getLogger(BridgeEngine.class);

    /** 防环标识：入向消息以此 ClientID 注入本地，出向拦截时识别并丢弃 */
    public static final String BRIDGE_INBOUND_AGENT = "BRIDGE_INBOUND_AGENT";

    private final String bridgeId;
    private final String name;
    private final String remoteUrl;
    private final String clientId;
    private final int keepAlive;
    private final int connectionTimeout;
    private final String username;
    private final String password;
    private final Server localBroker;

    private volatile MqttConnectionOptions connectOptions;
    private volatile MqttAsyncClient pahoClient;
    private volatile String status = "DISCONNECTED";
    private volatile Instant lastConnectedTime;
    private volatile String lastError;
    private volatile List<RouteRule> routeRules = new CopyOnWriteArrayList<>();

    // 统计计数器
    private final AtomicLong sentCount = new AtomicLong(0);
    private final AtomicLong receivedCount = new AtomicLong(0);
    private final AtomicLong sentBytes = new AtomicLong(0);
    private final AtomicLong receivedBytes = new AtomicLong(0);

    // 出向消息队列
    private final BlockingQueue<OutboundMessage> outboundQueue = new LinkedBlockingQueue<>(10000);
    private volatile ExecutorService workerExecutor;
    private volatile boolean running = false;

    // 自动重连调度
    private volatile ScheduledExecutorService reconnectScheduler;
    private final AtomicInteger reconnectAttempt = new AtomicInteger(0);
    private static final int MAX_RECONNECT_DELAY = 60; // 秒

    public BridgeEngine(String bridgeId, String name, String remoteUrl, String clientId,
                        int keepAlive, int connectionTimeout, String username, String password,
                        Server localBroker) {
        this.bridgeId = bridgeId;
        this.name = name;
        this.remoteUrl = remoteUrl;
        this.clientId = clientId;
        this.keepAlive = keepAlive;
        this.connectionTimeout = connectionTimeout;
        this.username = username;
        this.password = password;
        this.localBroker = localBroker;
    }

    /**
     * 启动桥接引擎
     */
    public synchronized void start() {
        if (running) {
            log.warn("桥接引擎已在运行: bridgeId={}", bridgeId);
            return;
        }
        running = true;
        reconnectAttempt.set(0);

        // 启动出向工作线程
        workerExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "bridge-out-" + bridgeId);
            t.setDaemon(true);
            return t;
        });
        workerExecutor.submit(this::outboundWorker);

        // 连接远程 Broker
        connectToRemote();

        log.info("桥接引擎已启动: bridgeId={}, name={}, remote={}", bridgeId, name, remoteUrl);
    }

    /**
     * 停止桥接引擎
     */
    public synchronized void stop() {
        if (!running) {
            return;
        }
        running = false;

        // 关闭 Paho 客户端
        if (pahoClient != null) {
            try {
                if (pahoClient.isConnected()) {
                    pahoClient.disconnect();
                }
                pahoClient.close();
            } catch (MqttException e) {
                log.warn("关闭 Paho 客户端异常: bridgeId={}", bridgeId, e);
            }
            pahoClient = null;
        }

        // 关闭工作线程
        if (workerExecutor != null) {
            workerExecutor.shutdownNow();
        }

        // 关闭重连调度器
        if (reconnectScheduler != null) {
            reconnectScheduler.shutdownNow();
        }

        status = "DISCONNECTED";
        log.info("桥接引擎已停止: bridgeId={}", bridgeId);
    }

    /**
     * 更新路由规则（热更新，无需重启）
     */
    public void updateRouteRules(List<RouteRule> rules) {
        this.routeRules = new CopyOnWriteArrayList<>(rules);
        // 重新订阅远程主题
        if (pahoClient != null && pahoClient.isConnected()) {
            subscribeInboundTopics();
        }
        log.info("路由规则已更新: bridgeId={}, count={}", bridgeId, rules.size());
    }

    /**
     * 出向拦截：本地 Broker 发布消息时调用
     * 由 BridgeInterceptHandler.onPublish() 触发
     */
    public void onLocalPublish(String topic, byte[] payload, int qos, boolean retained, String publisherClientId) {
        if (!running) {
            return;
        }
        // 防环：如果是入向注入的消息，不再外发
        if (BRIDGE_INBOUND_AGENT.equals(publisherClientId)) {
            return;
        }
        // 匹配出向规则
        for (RouteRule rule : routeRules) {
            if (!"outbound".equals(rule.getDirection())) {
                continue;
            }
            if (TopicMatcher.matches(rule.getSourceTopic(), topic)) {
                // 计算目的主题
                String destTopic = topic;
                if (rule.getDestTopic() != null && !rule.getDestTopic().isEmpty()) {
                    String[] wildcards = TopicMatcher.extractWildcards(rule.getSourceTopic(), topic);
                    destTopic = TopicMatcher.replaceVariables(rule.getDestTopic(), wildcards);
                }
                // 计算 QoS
                int effectiveQos = rule.getQos() >= 0 ? rule.getQos() : qos;
                // 处理 Retain
                boolean effectiveRetained = retained;
                if ("strip".equals(rule.getRetainHandling())) {
                    effectiveRetained = false;
                } else if ("ifRetained".equals(rule.getRetainHandling()) && !retained) {
                    continue; // 仅转发保留消息
                }
                // 投递到出向队列
                OutboundMessage msg = new OutboundMessage(destTopic, payload, effectiveQos, effectiveRetained);
                if (!outboundQueue.offer(msg)) {
                    log.warn("出向队列已满，丢弃消息: bridgeId={}, topic={}", bridgeId, destTopic);
                }
                break; // 一条消息只匹配第一条规则
            }
        }
    }

    /**
     * 连接远程 Broker
     */
    private void connectToRemote() {
        try {
            status = "RECONNECTING";
            // 关闭旧客户端，防止资源泄漏
            if (pahoClient != null) {
                try {
                    if (pahoClient.isConnected()) pahoClient.disconnect();
                    pahoClient.close();
                } catch (Exception e) {
                    log.debug("关闭旧 Paho 客户端异常: bridgeId={}", bridgeId);
                }
            }
            pahoClient = new MqttAsyncClient(remoteUrl, clientId, new MemoryPersistence());

            connectOptions = new MqttConnectionOptions();
            connectOptions.setAutomaticReconnect(false); // 我们自己管理重连
            connectOptions.setConnectionTimeout(connectionTimeout);
            connectOptions.setKeepAliveInterval(keepAlive);
            connectOptions.setCleanStart(true);

            if (username != null && !username.isEmpty()) {
                connectOptions.setUserName(username);
            }
            if (password != null && !password.isEmpty()) {
                connectOptions.setPassword(password.getBytes(StandardCharsets.UTF_8));
            }

            pahoClient.setCallback(new MqttCallback() {
                @Override
                public void disconnected(MqttDisconnectResponse response) {
                    status = "DISCONNECTED";
                    log.warn("远程连接断开: bridgeId={}, reason={}", bridgeId,
                            response != null ? response.getReasonString() : "unknown");
                    if (running) {
                        scheduleReconnect();
                    }
                }

                @Override
                public void mqttErrorOccurred(MqttException exception) {
                    log.error("MQTT 错误: bridgeId={}", bridgeId, exception);
                    lastError = exception.getMessage();
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    onRemoteMessageArrived(topic, message);
                }

                @Override
                public void deliveryComplete(IMqttToken token) {
                    // 出向投递完成
                }

                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    status = "CONNECTED";
                    lastConnectedTime = Instant.now();
                    lastError = null;
                    reconnectAttempt.set(0);
                    log.info("远程连接成功: bridgeId={}, uri={}, reconnect={}", bridgeId, serverURI, reconnect);
                    // 连接成功后订阅入向主题
                    subscribeInboundTopics();
                }

                @Override
                public void authPacketArrived(int reasonCode, org.eclipse.paho.mqttv5.common.packet.MqttProperties properties) {
                    // 认证包到达
                }
            });

            log.info("正在连接远程 Broker: bridgeId={}, url={}", bridgeId, remoteUrl);
            pahoClient.connect(connectOptions);

        } catch (MqttException e) {
            status = "FAILED";
            lastError = e.getMessage();
            log.error("连接远程 Broker 失败: bridgeId={}", bridgeId, e);
            if (running) {
                scheduleReconnect();
            }
        }
    }

    /**
     * 订阅入向规则定义的远程主题
     */
    private void subscribeInboundTopics() {
        List<RouteRule> inboundRules = routeRules.stream()
                .filter(r -> "inbound".equals(r.getDirection()))
                .toList();

        if (inboundRules.isEmpty()) {
            return;
        }

        String[] topics = inboundRules.stream()
                .map(RouteRule::getSourceTopic)
                .filter(t -> t != null && !t.isEmpty())
                .distinct()
                .toArray(String[]::new);

        int[] qosArray = new int[topics.length];
        for (int i = 0; i < topics.length; i++) {
            // 找到对应规则的 QoS
            int ruleQos = 0;
            for (RouteRule r : inboundRules) {
                if (topics[i].equals(r.getSourceTopic())) {
                    ruleQos = r.getQos() >= 0 ? r.getQos() : 0;
                    break;
                }
            }
            qosArray[i] = ruleQos;
        }

        try {
            pahoClient.subscribe(topics, qosArray);
            log.info("已订阅远程入向主题: bridgeId={}, topics={}", bridgeId, String.join(", ", topics));
        } catch (MqttException e) {
            log.error("订阅远程主题失败: bridgeId={}", bridgeId, e);
        }
    }

    /**
     * 远程消息到达回调（入向处理）
     */
    private void onRemoteMessageArrived(String topic, MqttMessage message) {
        if (!running) {
            return;
        }
        byte[] payload = message.getPayload();
        int qos = message.getQos();
        boolean retained = message.isRetained();

        // 匹配入向规则
        for (RouteRule rule : routeRules) {
            if (!"inbound".equals(rule.getDirection())) {
                continue;
            }
            if (TopicMatcher.matches(rule.getSourceTopic(), topic)) {
                // 计算目的主题
                String destTopic = topic;
                if (rule.getDestTopic() != null && !rule.getDestTopic().isEmpty()) {
                    String[] wildcards = TopicMatcher.extractWildcards(rule.getSourceTopic(), topic);
                    destTopic = TopicMatcher.replaceVariables(rule.getDestTopic(), wildcards);
                }
                // 计算 QoS
                int effectiveQos = rule.getQos() >= 0 ? rule.getQos() : qos;
                // 处理 Retain
                boolean effectiveRetained = retained;
                if ("strip".equals(rule.getRetainHandling())) {
                    effectiveRetained = false;
                } else if ("ifRetained".equals(rule.getRetainHandling()) && !retained) {
                    continue;
                }
                // 注入本地 Broker（使用 internalPublish 避免再次触发拦截器的出向逻辑）
                injectToLocalBroker(destTopic, payload, effectiveQos, effectiveRetained);

                receivedCount.incrementAndGet();
                receivedBytes.addAndGet(payload.length);
                break;
            }
        }
    }

    /**
     * 将消息注入本地 Broker
     */
    private void injectToLocalBroker(String topic, byte[] payload, int qos, boolean retained) {
        try {
            MqttFixedHeader fixedHeader = new MqttFixedHeader(
                    MqttMessageType.PUBLISH, false,
                    MqttQoS.valueOf(qos), retained, 0);
            MqttPublishVariableHeader variableHeader = new MqttPublishVariableHeader(topic, 0);
            ByteBuf byteBuf = Unpooled.wrappedBuffer(payload);
            MqttPublishMessage publishMessage = new MqttPublishMessage(fixedHeader, variableHeader, byteBuf);

            localBroker.internalPublish(publishMessage, BRIDGE_INBOUND_AGENT);
            log.debug("入向消息已注入本地: bridgeId={}, topic={}", bridgeId, topic);
        } catch (Exception e) {
            log.error("注入本地 Broker 失败: bridgeId={}, topic={}", bridgeId, topic, e);
        }
    }

    /**
     * 出向工作线程：从队列消费消息并发送到远程
     */
    private void outboundWorker() {
        while (running) {
            try {
                OutboundMessage msg = outboundQueue.poll(1, TimeUnit.SECONDS);
                if (msg == null) {
                    continue;
                }
                if (pahoClient == null || !pahoClient.isConnected()) {
                    // 未连接时丢弃（队列有上限，背压策略：丢弃最老消息）
                    if (outboundQueue.size() > 5000) {
                        OutboundMessage dropped = outboundQueue.poll();
                        if (dropped != null) {
                            log.warn("背压丢弃出向消息: bridgeId={}, topic={}", bridgeId, dropped.topic);
                        }
                    }
                    continue;
                }
                try {
                    MqttMessage mqttMsg = new MqttMessage(msg.payload);
                    mqttMsg.setQos(msg.qos);
                    mqttMsg.setRetained(msg.retained);
                    pahoClient.publish(msg.topic, mqttMsg);

                    sentCount.incrementAndGet();
                    sentBytes.addAndGet(msg.payload.length);
                    log.debug("出向消息已发送: bridgeId={}, topic={}", bridgeId, msg.topic);
                } catch (MqttException e) {
                    log.error("出向消息发送失败: bridgeId={}, topic={}", bridgeId, msg.topic, e);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * 指数退避重连调度
     */
    private void scheduleReconnect() {
        if (!running) {
            return;
        }
        if (reconnectScheduler == null || reconnectScheduler.isShutdown()) {
            reconnectScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "bridge-reconnect-" + bridgeId);
                t.setDaemon(true);
                return t;
            });
        }
        int attempt = reconnectAttempt.getAndIncrement();
        int delay = Math.min((int) Math.pow(2, attempt), MAX_RECONNECT_DELAY);
        log.info("将在 {} 秒后重连: bridgeId={}, attempt={}", delay, bridgeId, reconnectAttempt);
        reconnectScheduler.schedule(this::connectToRemote, delay, TimeUnit.SECONDS);
    }

    // ============ 状态与统计 ============

    public String getBridgeId() {
        return bridgeId;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public Instant getLastConnectedTime() {
        return lastConnectedTime;
    }

    public String getLastError() {
        return lastError;
    }

    public long getSentCount() {
        return sentCount.get();
    }

    public long getReceivedCount() {
        return receivedCount.get();
    }

    public long getSentBytes() {
        return sentBytes.get();
    }

    public long getReceivedBytes() {
        return receivedBytes.get();
    }

    public boolean isRunning() {
        return running;
    }

    /**
     * 出向消息内部模型
     */
    private static class OutboundMessage {
        final String topic;
        final byte[] payload;
        final int qos;
        final boolean retained;

        OutboundMessage(String topic, byte[] payload, int qos, boolean retained) {
            this.topic = topic;
            this.payload = payload;
            this.qos = qos;
            this.retained = retained;
        }
    }
}
