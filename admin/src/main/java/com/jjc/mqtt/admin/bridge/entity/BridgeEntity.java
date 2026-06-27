package com.jjc.mqtt.admin.bridge.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

/**
 * 桥接链路配置实体
 *
 * @author sweeter
 */
@Data
@Entity
@Table(name = "mqtt_bridge", indexes = {
    @Index(name = "idx_bridge_name", columnList = "name")
})
public class BridgeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 链路别名 */
    @Column(nullable = false, length = 100)
    private String name;

    /** 远程 Broker 地址 */
    @Column(nullable = false, length = 512)
    private String remoteUrl;

    /** MQTT Client ID */
    @Column(nullable = false, length = 128)
    private String clientId;

    /** Keep Alive 间隔（秒） */
    @Column(nullable = false)
    private Integer keepAlive = 60;

    /** 连接超时（秒） */
    @Column(nullable = false)
    private Integer connectionTimeout = 10;

    /** 默认 QoS */
    @Column(nullable = false)
    private Integer defaultQos = 0;

    /** 认证方式: anonymous / password / ssl */
    @Column(nullable = false, length = 20)
    private String authType = "anonymous";

    /** 用户名 */
    @Column(length = 128)
    private String username;

    /** 密码 */
    @Column(length = 256)
    private String password;

    /** CA 证书 */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String caCert;

    /** 客户端证书 */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String clientCert;

    /** 客户端密钥 */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String clientKey;

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
