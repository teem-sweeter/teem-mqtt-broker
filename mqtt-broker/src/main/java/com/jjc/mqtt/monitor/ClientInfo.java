package com.jjc.mqtt.monitor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

public class ClientInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String clientId;
    private String username;
    private String ipAddress;
    private int port;
    private int keepAlive;
    private long connectTime;
    private Set<String> subscriptions;
    // 订阅详情
    private Set<SubscriptionInfo> subscriptionDetails;
    // 连接信息
    private String protocolVersion;
    private boolean cleanSession;
    // 遗嘱信息
    private boolean willFlag;
    private String willTopic;
    private String willMessage;
    private int willQos;
    private boolean willRetain;
    // 运行指标
    private int inflightCount;
    private int queuedCount;

    public ClientInfo() {
    }

    // ===== Getters & Setters =====

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public int getKeepAlive() { return keepAlive; }
    public void setKeepAlive(int keepAlive) { this.keepAlive = keepAlive; }

    public long getConnectTime() { return connectTime; }
    public void setConnectTime(long connectTime) { this.connectTime = connectTime; }

    public Set<String> getSubscriptions() { return subscriptions; }
    public void setSubscriptions(Set<String> subscriptions) { this.subscriptions = subscriptions; }

    public Set<SubscriptionInfo> getSubscriptionDetails() { return subscriptionDetails; }
    public void setSubscriptionDetails(Set<SubscriptionInfo> subscriptionDetails) { this.subscriptionDetails = subscriptionDetails; }

    public String getProtocolVersion() { return protocolVersion; }
    public void setProtocolVersion(String protocolVersion) { this.protocolVersion = protocolVersion; }

    public boolean isCleanSession() { return cleanSession; }
    public void setCleanSession(boolean cleanSession) { this.cleanSession = cleanSession; }

    public boolean isWillFlag() { return willFlag; }
    public void setWillFlag(boolean willFlag) { this.willFlag = willFlag; }

    public String getWillTopic() { return willTopic; }
    public void setWillTopic(String willTopic) { this.willTopic = willTopic; }

    public String getWillMessage() { return willMessage; }
    public void setWillMessage(String willMessage) { this.willMessage = willMessage; }

    public int getWillQos() { return willQos; }
    public void setWillQos(int willQos) { this.willQos = willQos; }

    public boolean isWillRetain() { return willRetain; }
    public void setWillRetain(boolean willRetain) { this.willRetain = willRetain; }

    public int getInflightCount() { return inflightCount; }
    public void setInflightCount(int inflightCount) { this.inflightCount = inflightCount; }

    public int getQueuedCount() { return queuedCount; }
    public void setQueuedCount(int queuedCount) { this.queuedCount = queuedCount; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ClientInfo info = new ClientInfo();

        public Builder clientId(String v) { info.clientId = v; return this; }
        public Builder username(String v) { info.username = v; return this; }
        public Builder ipAddress(String v) { info.ipAddress = v; return this; }
        public Builder port(int v) { info.port = v; return this; }
        public Builder keepAlive(int v) { info.keepAlive = v; return this; }
        public Builder connectTime(long v) { info.connectTime = v; return this; }
        public Builder subscriptions(Set<String> v) { info.subscriptions = v; return this; }
        public Builder subscriptionDetails(Set<SubscriptionInfo> v) { info.subscriptionDetails = v; return this; }
        public Builder protocolVersion(String v) { info.protocolVersion = v; return this; }
        public Builder cleanSession(boolean v) { info.cleanSession = v; return this; }
        public Builder willFlag(boolean v) { info.willFlag = v; return this; }
        public Builder willTopic(String v) { info.willTopic = v; return this; }
        public Builder willMessage(String v) { info.willMessage = v; return this; }
        public Builder willQos(int v) { info.willQos = v; return this; }
        public Builder willRetain(boolean v) { info.willRetain = v; return this; }
        public Builder inflightCount(int v) { info.inflightCount = v; return this; }
        public Builder queuedCount(int v) { info.queuedCount = v; return this; }

        public ClientInfo build() { return info; }
    }
}
