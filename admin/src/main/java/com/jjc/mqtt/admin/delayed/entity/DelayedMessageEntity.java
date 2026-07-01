package com.jjc.mqtt.admin.delayed.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Data
@Entity
@Table(name = "delayed_message", indexes = {
    @Index(name = "idx_dm_status", columnList = "status"),
    @Index(name = "idx_dm_scheduled", columnList = "scheduled_time")
})
public class DelayedMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1024)
    private String topic;

    @Column(nullable = false)
    private Integer qos;

    @Column(nullable = false)
    private Boolean retain = false;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String payload;

    @Column(name = "delay_seconds", nullable = false)
    private Integer delaySeconds;

    @Column(name = "scheduled_time", nullable = false)
    private Instant scheduledTime;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;
}
