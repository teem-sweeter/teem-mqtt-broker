package com.jjc.mqtt.admin.client.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

/**
 * 客户端管控规则实体
 *
 * @author sweeter
 */
@Data
@Entity
@Table(name = "mqtt_client_rule", indexes = {
    @Index(name = "idx_cr_client_id", columnList = "clientId", unique = true)
})
public class ClientRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 客户端ID */
    @Column(nullable = false, unique = true, length = 128)
    private String clientId;

    /** 是否禁止发送消息（拦截该客户端的发布） */
    @Column(nullable = false)
    private Boolean sendDisabled = false;

    /** 是否禁止接收消息（不向该客户端投递消息） */
    @Column(nullable = false)
    private Boolean receiveDisabled = false;

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
