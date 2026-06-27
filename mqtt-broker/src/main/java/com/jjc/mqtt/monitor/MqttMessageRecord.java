package com.jjc.mqtt.monitor;

import java.io.Serial;
import java.io.Serializable;

public class MqttMessageRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String topic;
    private String payload;
    private int qos;
    private String clientId;
    private long timestamp;
    private String direction;

    public MqttMessageRecord() {
    }

    public MqttMessageRecord(String topic, String payload, int qos, String clientId,
                             long timestamp, String direction) {
        this.topic = topic;
        this.payload = payload;
        this.qos = qos;
        this.clientId = clientId;
        this.timestamp = timestamp;
        this.direction = direction;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String topic;
        private String payload;
        private int qos;
        private String clientId;
        private long timestamp;
        private String direction;

        public Builder topic(String topic) {
            this.topic = topic;
            return this;
        }

        public Builder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public Builder qos(int qos) {
            this.qos = qos;
            return this;
        }

        public Builder clientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder direction(String direction) {
            this.direction = direction;
            return this;
        }

        public MqttMessageRecord build() {
            return new MqttMessageRecord(topic, payload, qos, clientId, timestamp, direction);
        }
    }
}