package com.jjc.mqtt.admin.client.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

/**
 * MQTT ACL 规则实体
 *
 * @author sweeter
 */
@Data
@Entity
@Table(name = "mqtt_acl_rule", indexes = {
    @Index(name = "idx_mar_client_id", columnList = "clientId"),
    @Index(name = "idx_mar_username", columnList = "username")
})
public class MqttAclRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 客户端ID (为空表示适用于所有客户端) */
    @Column(length = 128)
    private String clientId;

    /** 用户名 (为空表示适用于所有用户) */
    @Column(length = 100)
    private String username;

    /** 主题过滤器 (如 sensor/+/temperature) */
    @Column(nullable = false, length = 256)
    private String topic;

    /** 权限类型: PUB (发布), SUB (订阅), PUB_SUB (发布及订阅) */
    @Column(nullable = false, length = 20)
    private String permission;

    /** 动作类型: ALLOW (允许), DENY (拒绝) */
    @Column(nullable = false, length = 20)
    private String action;

    /** 优先级 (数值越大优先级越高) */
    @Column(nullable = false)
    private Integer priority = 0;

    /** 备注 */
    @Column(length = 500)
    private String remark;

    /** 创建时间 */
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /** 更新时间 */
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
}
