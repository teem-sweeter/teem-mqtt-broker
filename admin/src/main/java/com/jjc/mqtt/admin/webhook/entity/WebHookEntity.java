package com.jjc.mqtt.admin.webhook.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "mqtt_webhook", indexes = {
    @Index(name = "idx_webhook_name", columnList = "name")
})
public class WebHookEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 1024)
    private String url;

    @Column(length = 100)
    private String topicFilter;

    @Column(nullable = false)
    private Integer qos = 0;

    @Column(columnDefinition = "TEXT")
    private String headers;

    @Column(length = 50)
    private String contentType = "application/json";

    @Column(nullable = false)
    private Integer retryCount = 0;

    @Column(nullable = false)
    private Integer retryInterval = 5;

    @Column(nullable = false)
    private Integer connectTimeout = 10;

    @Column(nullable = false)
    private Integer readTimeout = 30;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(length = 500)
    private String remark;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getTopicFilter() { return topicFilter; }
    public void setTopicFilter(String topicFilter) { this.topicFilter = topicFilter; }
    public Integer getQos() { return qos; }
    public void setQos(Integer qos) { this.qos = qos; }
    public String getHeaders() { return headers; }
    public void setHeaders(String headers) { this.headers = headers; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    public Integer getRetryInterval() { return retryInterval; }
    public void setRetryInterval(Integer retryInterval) { this.retryInterval = retryInterval; }
    public Integer getConnectTimeout() { return connectTimeout; }
    public void setConnectTimeout(Integer connectTimeout) { this.connectTimeout = connectTimeout; }
    public Integer getReadTimeout() { return readTimeout; }
    public void setReadTimeout(Integer readTimeout) { this.readTimeout = readTimeout; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
