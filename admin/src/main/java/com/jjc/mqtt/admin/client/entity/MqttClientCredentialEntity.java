package com.jjc.mqtt.admin.client.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

/**
 * MQTT 客户端认证凭证实体
 *
 * @author sweeter
 */
@Data
@Entity
@Table(name = "mqtt_client_credential", indexes = {
    @Index(name = "idx_mcc_client_id", columnList = "clientId", unique = true),
    @Index(name = "idx_mcc_username", columnList = "username")
})
public class MqttClientCredentialEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 客户端ID (唯一) */
    @Column(nullable = false, unique = true, length = 128)
    private String clientId;

    /** 用户名 */
    @Column(nullable = false, length = 100)
    private String username;

    /** 加密后的密码 (BCrypt) */
    @Column(nullable = false, length = 100)
    private String password;

    /** 是否启用 */
    @Column(nullable = false)
    private Boolean enabled = true;

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
