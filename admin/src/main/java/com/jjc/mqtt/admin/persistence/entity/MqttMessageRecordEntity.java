package com.jjc.mqtt.admin.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Data
@Entity
@Table(name = "mqtt_message_record", indexes = {
    @Index(name = "idx_topic", columnList = "topic"),
    @Index(name = "idx_client_id", columnList = "client_id"),
    @Index(name = "idx_timestamp", columnList = "timestamp")
})
public class MqttMessageRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1024)
    private String topic;

    @Column(length = 512)
    private String clientId;

    @Column(length = 20)
    private String direction;

    @Column(nullable = false)
    private Integer qos;

    @Column(nullable = false)
    private Boolean retained = false;

    @Column(nullable = false)
    private Instant timestamp;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String payload;
}
