package com.jjc.mqtt.event;

import java.io.Serial;
import java.io.Serializable;

public class MqttMessageEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String topic;
    private String payload;
    private long timestamp;
    private int qos;
    private String clientId;

    public MqttMessageEvent() {
    }

    public MqttMessageEvent(String topic, String payload, long timestamp, int qos) {
        this.topic = topic;
        this.payload = payload;
        this.timestamp = timestamp;
        this.qos = qos;
    }

    public MqttMessageEvent(String topic, String payload, long timestamp, int qos, String clientId) {
        this.topic = topic;
        this.payload = payload;
        this.timestamp = timestamp;
        this.qos = qos;
        this.clientId = clientId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getQos() {
        return qos;
    }

    public void setQos(int qos) {
        this.qos = qos;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String toJsonLine() {
        return String.format(
            "{\"ts\":%d,\"topic\":\"%s\",\"qos\":%d,\"clientId\":\"%s\",\"data\":%s}",
            timestamp,
            escapeJson(topic),
            qos,
            escapeJson(clientId),
            payload
        );
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