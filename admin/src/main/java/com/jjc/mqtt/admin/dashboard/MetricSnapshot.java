package com.jjc.mqtt.admin.dashboard;

import java.util.List;

public class MetricSnapshot {
    private long timestamp;
    private long publishDelta;
    private long receiveDelta;
    private long bytesInDelta;
    private long bytesOutDelta;
    private int activeClients;
    private long qos0Delta;
    private long qos1Delta;
    private long qos2Delta;
    private long errorsDelta;
    private List<TopicCount> topTopics;

    public MetricSnapshot() {}

    public MetricSnapshot(long timestamp, long publishDelta, long receiveDelta,
                          long bytesInDelta, long bytesOutDelta, int activeClients,
                          long qos0Delta, long qos1Delta, long qos2Delta,
                          long errorsDelta, List<TopicCount> topTopics) {
        this.timestamp = timestamp;
        this.publishDelta = publishDelta;
        this.receiveDelta = receiveDelta;
        this.bytesInDelta = bytesInDelta;
        this.bytesOutDelta = bytesOutDelta;
        this.activeClients = activeClients;
        this.qos0Delta = qos0Delta;
        this.qos1Delta = qos1Delta;
        this.qos2Delta = qos2Delta;
        this.errorsDelta = errorsDelta;
        this.topTopics = topTopics;
    }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public long getPublishDelta() { return publishDelta; }
    public void setPublishDelta(long publishDelta) { this.publishDelta = publishDelta; }
    public long getReceiveDelta() { return receiveDelta; }
    public void setReceiveDelta(long receiveDelta) { this.receiveDelta = receiveDelta; }
    public long getBytesInDelta() { return bytesInDelta; }
    public void setBytesInDelta(long bytesInDelta) { this.bytesInDelta = bytesInDelta; }
    public long getBytesOutDelta() { return bytesOutDelta; }
    public void setBytesOutDelta(long bytesOutDelta) { this.bytesOutDelta = bytesOutDelta; }
    public int getActiveClients() { return activeClients; }
    public void setActiveClients(int activeClients) { this.activeClients = activeClients; }
    public long getQos0Delta() { return qos0Delta; }
    public void setQos0Delta(long qos0Delta) { this.qos0Delta = qos0Delta; }
    public long getQos1Delta() { return qos1Delta; }
    public void setQos1Delta(long qos1Delta) { this.qos1Delta = qos1Delta; }
    public long getQos2Delta() { return qos2Delta; }
    public void setQos2Delta(long qos2Delta) { this.qos2Delta = qos2Delta; }
    public long getErrorsDelta() { return errorsDelta; }
    public void setErrorsDelta(long errorsDelta) { this.errorsDelta = errorsDelta; }
    public List<TopicCount> getTopTopics() { return topTopics; }
    public void setTopTopics(List<TopicCount> topTopics) { this.topTopics = topTopics; }

    public static class TopicCount {
        private String topic;
        private long count;

        public TopicCount() {}

        public TopicCount(String topic, long count) {
            this.topic = topic;
            this.count = count;
        }

        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
    }
}
