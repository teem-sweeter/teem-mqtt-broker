package com.jjc.mqtt.handler;

import com.jjc.mqtt.ConnectedClients;
import com.jjc.mqtt.monitor.MonitorService;
import com.jjc.mqtt.monitor.SubscriptionInfo;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.messages.InterceptConnectMessage;
import io.moquette.interception.messages.InterceptDisconnectMessage;
import io.moquette.interception.messages.InterceptConnectionLostMessage;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.moquette.interception.messages.InterceptSubscribeMessage;
import io.moquette.interception.messages.InterceptUnsubscribeMessage;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MQTT 监控处理器
 *
 * @author sweeter
 */
public class MqttMonitorHandler extends AbstractInterceptHandler {

    private static final Logger log = LoggerFactory.getLogger(MqttMonitorHandler.class);

    private final MonitorService monitorService;
    private final ConnectedClients connectedClients;

    private static final Map<Integer, String> clientIpMap = new ConcurrentHashMap<>();
    private static final Map<Integer, Integer> clientPortMap = new ConcurrentHashMap<>();

    public MqttMonitorHandler(MonitorService monitorService, ConnectedClients connectedClients) {
        this.monitorService = monitorService;
        this.connectedClients = connectedClients;
    }

    @Override
    public String getID() {
        return "mqtt_monitor_handler";
    }

    @Override
    public void onConnect(InterceptConnectMessage msg) {
        String clientId = msg.getClientID();
        String username = msg.getUsername();
        int keepAlive = msg.getKeepAlive();

        // 协议版本
        int protocolLevel = msg.getProtocolVersion();
        String protocolVersion = resolveProtocolVersion(protocolLevel);

        // Clean Session
        boolean cleanSession = msg.isCleanSession();

        // 遗嘱信息
        boolean willFlag = msg.isWillFlag();
        String willTopic = msg.getWillTopic();
        String willMessage = null;
        if (msg.getWillMessage() != null) {
            willMessage = new String(msg.getWillMessage(), StandardCharsets.UTF_8);
        }
        int willQos = msg.getWillQos();
        boolean willRetain = msg.isWillRetain();

        log.info("客户端连接: ClientID={}, Username={}, KeepAlive={}, Protocol={}, CleanSession={}, Will={}",
                clientId, username, keepAlive, protocolVersion, cleanSession, willFlag);

        monitorService.onClientConnect(clientId, username, keepAlive,
                protocolVersion, cleanSession,
                willFlag, willTopic, willMessage, willQos, willRetain);

        connectedClients.add(clientId);
    }

    @Override
    public void onDisconnect(InterceptDisconnectMessage msg) {
        String clientId = msg.getClientID();
        log.info("客户端断开: ClientID={}", clientId);
        monitorService.onClientDisconnect(clientId);
        connectedClients.remove(clientId);
    }

    @Override
    public void onConnectionLost(InterceptConnectionLostMessage msg) {
        String clientId = msg.getClientID();
        log.warn("客户端连接丢失: ClientID={}", clientId);
        monitorService.onClientDisconnect(clientId);
        connectedClients.remove(clientId);
    }

    @Override
    public void onPublish(InterceptPublishMessage msg) {
        try {
            String topic = msg.getTopicName();
            ByteBuf buf = msg.getPayload();
            byte[] bytes = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), bytes);
            String payload = isPrintableText(bytes)
                    ? new String(bytes, StandardCharsets.UTF_8)
                    : bytesToHex(bytes);
            String clientId = msg.getClientID();
            int qos = msg.getQos() != null ? msg.getQos().value() : 0;

            log.debug("消息发布: Topic={}, ClientID={}, QoS={}", topic, clientId, qos);
            monitorService.onMessagePublish(topic, payload, qos, clientId);
        } finally {
            io.netty.util.ReferenceCountUtil.safeRelease(msg.getPayload());
        }
    }

    @Override
    public void onSubscribe(InterceptSubscribeMessage msg) {
        String clientId = msg.getClientID();
        String topic = msg.getTopicFilter();
        int qos = msg.getRequestedQos().value();

        // 订阅选项
        boolean noLocal = false;
        boolean retainAsPublished = false;
        String shareName = null;
        try {
            var option = msg.getOption();
            noLocal = option.isNoLocal();
            retainAsPublished = option.isRetainAsPublished();
        } catch (Exception ignored) {
            // MQTT 3.x 不支持这些选项
        }

        SubscriptionInfo subInfo = new SubscriptionInfo(topic, qos, noLocal, retainAsPublished, shareName);

        log.info("客户端订阅: ClientID={}, Topic={}, QoS={}, NoLocal={}", clientId, topic, qos, noLocal);
        monitorService.onTopicSubscribe(clientId, subInfo);
    }

    @Override
    public void onUnsubscribe(InterceptUnsubscribeMessage msg) {
        String clientId = msg.getClientID();
        String topic = msg.getTopicFilter();
        log.info("客户端取消订阅: ClientID={}, Topic={}", clientId, topic);
        monitorService.onTopicUnsubscribe(clientId, topic);
    }

    @Override
    public void onSessionLoopError(Throwable throwable) {
        log.error("会话循环错误", throwable);
    }

    private String resolveProtocolVersion(int level) {
        return switch (level) {
            case 3 -> "MQTT 3.1";
            case 4 -> "MQTT 3.1.1";
            case 5 -> "MQTT 5.0";
            default -> "MQTT (" + level + ")";
        };
    }

    public static void registerChannelIp(int clientIdHash, String ip, int port) {
        clientIpMap.put(clientIdHash, ip);
        clientPortMap.put(clientIdHash, port);
    }

    public static void unregisterChannel(int clientIdHash) {
        clientIpMap.remove(clientIdHash);
        clientPortMap.remove(clientIdHash);
    }

    private static boolean isPrintableText(byte[] bytes) {
        for (byte b : bytes) {
            int v = b & 0xFF;
            if (v < 0x20 && v != 0x09 && v != 0x0A && v != 0x0D) {
                return false;
            }
            if (v > 0x7E) {
                return false;
            }
        }
        return true;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02X", b & 0xFF));
        }
        return sb.toString();
    }
}
