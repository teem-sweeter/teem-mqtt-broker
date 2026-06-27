package com.jjc.mqtt.admin.bridge.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 桥接路由规则实体
 *
 * @author sweeter
 */
@Data
@Entity
@Table(name = "mqtt_bridge_route_rule", indexes = {
    @Index(name = "idx_route_bridge_id", columnList = "bridge_id")
})
public class BridgeRouteRuleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 关联的桥接链路 ID */
    @Column(name = "bridge_id", nullable = false)
    private Long bridgeId;

    /** 方向: outbound / inbound */
    @Column(nullable = false, length = 20)
    private String direction;

    /** 源主题 */
    @Column(nullable = false, length = 1024)
    private String sourceTopic;

    /** 目的主题 */
    @Column(length = 1024)
    private String destTopic;

    /** QoS: -1 表示透传, 0/1/2 表示强制 */
    @Column(nullable = false)
    private Integer qos = -1;

    /** Retain 处理: keep / strip / ifRetained */
    @Column(nullable = false, length = 20)
    private String retainHandling = "keep";

    /** 排序序号 */
    @Column(nullable = false)
    private Integer sortOrder = 0;
}
