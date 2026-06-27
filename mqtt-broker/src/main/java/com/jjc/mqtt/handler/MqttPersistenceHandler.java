package com.jjc.mqtt.handler;

import com.jjc.mqtt.event.MqttMessageEvent;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.messages.InterceptConnectMessage;
import io.moquette.interception.messages.InterceptConnectionLostMessage;
import io.moquette.interception.messages.InterceptDisconnectMessage;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttPersistenceHandler extends AbstractInterceptHandler {

    private static final Logger log = LoggerFactory.getLogger(MqttPersistenceHandler.class);

    private static final Logger DISK_LOGGER = LoggerFactory.getLogger("MQTT_DISK_RECORDER");

    private static final String HANDLER_ID = "MqttPersistenceHandler";

    private static final long SAMPLE_INTERVAL = 1000L;

    private static volatile long totalMessageCount = 0L;

    @Override
    public String getID() {
        return HANDLER_ID;
    }

    @Override
    public void onPublish(InterceptPublishMessage msg) {
        long startTime = System.nanoTime();
        try {
            String topic = msg.getTopicName();
            MqttQoS qos = msg.getQos();
            String clientId = msg.getClientID();

            ByteBuf byteBuf = msg.getPayload();
            if (byteBuf == null || !byteBuf.isReadable()) {
                log.warn("收到空消息: topic={}, clientId={}", topic, clientId);
                return;
            }

            byte[] bytes = new byte[byteBuf.readableBytes()];
            byteBuf.getBytes(byteBuf.readerIndex(), bytes);
            String payload = isPrintableText(bytes)
                    ? "\"" + escapeJson(new String(bytes, java.nio.charset.StandardCharsets.UTF_8)) + "\""
                    : "\"" + bytesToHex(bytes) + "\"";
            long timestamp = System.currentTimeMillis();

            MqttMessageEvent event = new MqttMessageEvent(topic, payload, timestamp, qos.value(), clientId);

            DISK_LOGGER.info(event.toJsonLine());

            long count = ++totalMessageCount;
            if (count % SAMPLE_INTERVAL == 0) {
                long elapsed = System.nanoTime() - startTime;
                log.debug("MQTT 消息持久化采样: count={}, elapsed={}ns, topic={}", count, elapsed, topic);
            }

        } catch (Exception e) {
            log.error("MQTT 消息持久化预处理失败: topic={}, error={}", msg.getTopicName(), e.getMessage(), e);
        }
    }

    @Override
    public void onConnect(InterceptConnectMessage msg) {
        log.info("MQTT 客户端连接: clientId={}, keepAlive={}", msg.getClientID(), msg.getKeepAlive());
        LoggerFactory.getLogger("MQTT_CONNECTION_LOGGER")
            .info("{\"event\":\"connect\",\"clientId\":\"{}\",\"ts\":{}}",
                msg.getClientID(), System.currentTimeMillis());
    }

    @Override
    public void onDisconnect(InterceptDisconnectMessage msg) {
        log.info("MQTT 客户端断开: clientId={}", msg.getClientID());
        LoggerFactory.getLogger("MQTT_CONNECTION_LOGGER")
            .info("{\"event\":\"disconnect\",\"clientId\":\"{}\",\"ts\":{}}",
                msg.getClientID(), System.currentTimeMillis());
    }

    @Override
    public void onConnectionLost(InterceptConnectionLostMessage msg) {
        log.warn("MQTT 客户端连接丢失: clientId={}, Username={}", msg.getClientID(), msg.getUsername());
    }

    @Override
    public void onSessionLoopError(Throwable throwable) {
        log.error("MQTT 会话循环错误", throwable);
    }

    public static long getTotalMessageCount() {
        return totalMessageCount;
    }

    public static void resetCounter() {
        totalMessageCount = 0L;
    }

    private static boolean isPrintableText(byte[] bytes) {
        for (byte b : bytes) {
            int v = b & 0xFF;
            // 允许常见文本字符：空格(0x20)到波浪号(0x7E)，以及常见控制字符
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

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < ' ') {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
}