package com.jjc.mqtt.admin.bridge.model;

import java.time.Instant;
import java.util.List;

/**
 * 桥接链路 DTO
 *
 * @author sweeter
 */
public class Bridge {

    private Long id;
    private String name;
    private String remoteUrl;
    private String clientId;
    private Integer keepAlive;
    private Integer connectionTimeout;
    private Integer defaultQos;
    private String authType;
    private String username;
    private String password;
    private String caCert;
    private String clientCert;
    private String clientKey;
    private Boolean enabled;
    private String remark;
    private Instant createdAt;
    private Instant updatedAt;

    // 运行时状态（非持久化）
    private String status;
    private Long sentCount;
    private Long receivedCount;
    private Long sentBytes;
    private Long receivedBytes;
    private Instant lastConnectedTime;
    private String lastError;
    private Integer routeRuleCount;

    private List<RouteRule> routeRules;

    public Bridge() {
    }

    // ===== Getters & Setters =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRemoteUrl() { return remoteUrl; }
    public void setRemoteUrl(String remoteUrl) { this.remoteUrl = remoteUrl; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public Integer getKeepAlive() { return keepAlive; }
    public void setKeepAlive(Integer keepAlive) { this.keepAlive = keepAlive; }

    public Integer getConnectionTimeout() { return connectionTimeout; }
    public void setConnectionTimeout(Integer connectionTimeout) { this.connectionTimeout = connectionTimeout; }

    public Integer getDefaultQos() { return defaultQos; }
    public void setDefaultQos(Integer defaultQos) { this.defaultQos = defaultQos; }

    public String getAuthType() { return authType; }
    public void setAuthType(String authType) { this.authType = authType; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getCaCert() { return caCert; }
    public void setCaCert(String caCert) { this.caCert = caCert; }

    public String getClientCert() { return clientCert; }
    public void setClientCert(String clientCert) { this.clientCert = clientCert; }

    public String getClientKey() { return clientKey; }
    public void setClientKey(String clientKey) { this.clientKey = clientKey; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getSentCount() { return sentCount; }
    public void setSentCount(Long sentCount) { this.sentCount = sentCount; }

    public Long getReceivedCount() { return receivedCount; }
    public void setReceivedCount(Long receivedCount) { this.receivedCount = receivedCount; }

    public Long getSentBytes() { return sentBytes; }
    public void setSentBytes(Long sentBytes) { this.sentBytes = sentBytes; }

    public Long getReceivedBytes() { return receivedBytes; }
    public void setReceivedBytes(Long receivedBytes) { this.receivedBytes = receivedBytes; }

    public Instant getLastConnectedTime() { return lastConnectedTime; }
    public void setLastConnectedTime(Instant lastConnectedTime) { this.lastConnectedTime = lastConnectedTime; }

    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }

    public Integer getRouteRuleCount() { return routeRuleCount; }
    public void setRouteRuleCount(Integer routeRuleCount) { this.routeRuleCount = routeRuleCount; }

    public List<RouteRule> getRouteRules() { return routeRules; }
    public void setRouteRules(List<RouteRule> routeRules) { this.routeRules = routeRules; }
}
