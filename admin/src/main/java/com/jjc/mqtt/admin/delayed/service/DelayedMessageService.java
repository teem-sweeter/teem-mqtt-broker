package com.jjc.mqtt.admin.delayed.service;

import com.jjc.mqtt.admin.delayed.entity.DelayedMessageEntity;
import com.jjc.mqtt.admin.delayed.repository.DelayedMessageRepository;
import io.moquette.broker.Server;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DelayedMessageService {

    private static final Logger log = LoggerFactory.getLogger(DelayedMessageService.class);

    private final DelayedMessageRepository repository;
    private final Server mqttBroker;

    public DelayedMessageService(DelayedMessageRepository repository, Server mqttBroker) {
        this.repository = repository;
        this.mqttBroker = mqttBroker;
    }

    @Transactional
    public DelayedMessageEntity schedule(String topic, int qos, String payload, boolean retain, int delaySeconds) {
        DelayedMessageEntity entity = new DelayedMessageEntity();
        entity.setTopic(topic);
        entity.setQos(qos);
        entity.setPayload(payload);
        entity.setRetain(retain);
        entity.setDelaySeconds(delaySeconds);
        entity.setScheduledTime(Instant.now().plusSeconds(delaySeconds));
        entity.setStatus("PENDING");
        entity.setCreatedAt(Instant.now());
        entity = repository.save(entity);
        log.info("延迟消息已创建: id={}, topic={}, delay={}s, scheduledAt={}", entity.getId(), topic, delaySeconds, entity.getScheduledTime());
        return entity;
    }

    @Transactional
    public boolean cancel(Long id) {
        return repository.findById(id).map(entity -> {
            if (!"PENDING".equals(entity.getStatus())) {
                return false;
            }
            entity.setStatus("CANCELLED");
            repository.save(entity);
            log.info("延迟消息已取消: id={}", id);
            return true;
        }).orElse(false);
    }

    public List<DelayedMessageEntity> listPending() {
        return repository.findByStatusOrderByScheduledTimeAsc("PENDING");
    }

    public List<DelayedMessageEntity> listAll() {
        return repository.findAll();
    }

    public Map<String, Long> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("pending", repository.countByStatus("PENDING"));
        stats.put("delivered", repository.countByStatus("DELIVERED"));
        stats.put("cancelled", repository.countByStatus("CANCELLED"));
        return stats;
    }

    @Scheduled(fixedRate = 1000)
    @Transactional
    public void processDueMessages() {
        if (mqttBroker == null) return;

        List<DelayedMessageEntity> dueMessages = repository.findDueMessages(Instant.now());
        for (DelayedMessageEntity entity : dueMessages) {
            try {
                MqttQoS qos = MqttQoS.valueOf(entity.getQos());
                ByteBuf buf = Unpooled.copiedBuffer(entity.getPayload(), StandardCharsets.UTF_8);

                MqttPublishMessage publishMessage = MqttMessageBuilders.publish()
                        .topicName(entity.getTopic())
                        .qos(qos)
                        .payload(buf)
                        .build();

                if (entity.getRetain()) {
                    MqttFixedHeader fixedHeader = publishMessage.fixedHeader();
                    MqttFixedHeader newHeader = new MqttFixedHeader(
                            fixedHeader.messageType(), fixedHeader.isDup(), fixedHeader.qosLevel(),
                            true, fixedHeader.remainingLength());
                    publishMessage = new MqttPublishMessage(newHeader, publishMessage.variableHeader(), publishMessage.payload());
                }

                mqttBroker.internalPublish(publishMessage, "delayed-scheduler");

                entity.setStatus("DELIVERED");
                entity.setDeliveredAt(Instant.now());
                repository.save(entity);
                log.info("延迟消息已投递: id={}, topic={}", entity.getId(), entity.getTopic());
            } catch (Exception e) {
                log.error("延迟消息投递失败: id={}, topic={}", entity.getId(), entity.getTopic(), e);
            }
        }
    }
}
