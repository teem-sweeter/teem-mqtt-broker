package com.jjc.mqtt.monitor;

import java.io.Serial;
import java.io.Serializable;

public class MqttStats implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private long totalConnections;
    private long totalDisconnections;
    private long totalMessagesSent;
    private long totalMessagesReceived;
    private long currentConnections;
    private long totalSubscriptions;
    private long totalTopics;
    private double messagesPerSecond;
    private long uptime;

    public MqttStats() {
    }

    public MqttStats(long totalConnections, long totalDisconnections, long totalMessagesSent,
                     long totalMessagesReceived, long currentConnections, long totalSubscriptions,
                     long totalTopics, double messagesPerSecond, long uptime) {
        this.totalConnections = totalConnections;
        this.totalDisconnections = totalDisconnections;
        this.totalMessagesSent = totalMessagesSent;
        this.totalMessagesReceived = totalMessagesReceived;
        this.currentConnections = currentConnections;
        this.totalSubscriptions = totalSubscriptions;
        this.totalTopics = totalTopics;
        this.messagesPerSecond = messagesPerSecond;
        this.uptime = uptime;
    }

    public long getTotalConnections() {
        return totalConnections;
    }

    public void setTotalConnections(long totalConnections) {
        this.totalConnections = totalConnections;
    }

    public long getTotalDisconnections() {
        return totalDisconnections;
    }

    public void setTotalDisconnections(long totalDisconnections) {
        this.totalDisconnections = totalDisconnections;
    }

    public long getTotalMessagesSent() {
        return totalMessagesSent;
    }

    public void setTotalMessagesSent(long totalMessagesSent) {
        this.totalMessagesSent = totalMessagesSent;
    }

    public long getTotalMessagesReceived() {
        return totalMessagesReceived;
    }

    public void setTotalMessagesReceived(long totalMessagesReceived) {
        this.totalMessagesReceived = totalMessagesReceived;
    }

    public long getCurrentConnections() {
        return currentConnections;
    }

    public void setCurrentConnections(long currentConnections) {
        this.currentConnections = currentConnections;
    }

    public long getTotalSubscriptions() {
        return totalSubscriptions;
    }

    public void setTotalSubscriptions(long totalSubscriptions) {
        this.totalSubscriptions = totalSubscriptions;
    }

    public long getTotalTopics() {
        return totalTopics;
    }

    public void setTotalTopics(long totalTopics) {
        this.totalTopics = totalTopics;
    }

    public double getMessagesPerSecond() {
        return messagesPerSecond;
    }

    public void setMessagesPerSecond(double messagesPerSecond) {
        this.messagesPerSecond = messagesPerSecond;
    }

    public long getUptime() {
        return uptime;
    }

    public void setUptime(long uptime) {
        this.uptime = uptime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private long totalConnections;
        private long totalDisconnections;
        private long totalMessagesSent;
        private long totalMessagesReceived;
        private long currentConnections;
        private long totalSubscriptions;
        private long totalTopics;
        private double messagesPerSecond;
        private long uptime;

        public Builder totalConnections(long totalConnections) {
            this.totalConnections = totalConnections;
            return this;
        }

        public Builder totalDisconnections(long totalDisconnections) {
            this.totalDisconnections = totalDisconnections;
            return this;
        }

        public Builder totalMessagesSent(long totalMessagesSent) {
            this.totalMessagesSent = totalMessagesSent;
            return this;
        }

        public Builder totalMessagesReceived(long totalMessagesReceived) {
            this.totalMessagesReceived = totalMessagesReceived;
            return this;
        }

        public Builder currentConnections(long currentConnections) {
            this.currentConnections = currentConnections;
            return this;
        }

        public Builder totalSubscriptions(long totalSubscriptions) {
            this.totalSubscriptions = totalSubscriptions;
            return this;
        }

        public Builder totalTopics(long totalTopics) {
            this.totalTopics = totalTopics;
            return this;
        }

        public Builder messagesPerSecond(double messagesPerSecond) {
            this.messagesPerSecond = messagesPerSecond;
            return this;
        }

        public Builder uptime(long uptime) {
            this.uptime = uptime;
            return this;
        }

        public MqttStats build() {
            return new MqttStats(totalConnections, totalDisconnections, totalMessagesSent,
                    totalMessagesReceived, currentConnections, totalSubscriptions,
                    totalTopics, messagesPerSecond, uptime);
        }
    }
}