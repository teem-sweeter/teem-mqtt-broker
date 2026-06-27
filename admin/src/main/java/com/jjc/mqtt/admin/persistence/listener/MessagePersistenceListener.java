package com.jjc.mqtt.admin.persistence.listener;

import com.jjc.mqtt.MqttPersistenceProperties;
import com.jjc.mqtt.admin.persistence.entity.MqttMessageRecordEntity;
import com.jjc.mqtt.admin.persistence.repository.MqttMessageRecordRepository;
import com.jjc.mqtt.monitor.MonitorService;
import com.jjc.mqtt.monitor.MqttMessageRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class MessagePersistenceListener {

    private static final Logger log = LoggerFactory.getLogger(MessagePersistenceListener.class);

    private final MqttMessageRecordRepository messageRepository;
    private final MqttPersistenceProperties persistenceProperties;
    private final AtomicLong saveCount = new AtomicLong(0);
    private static final int CHECK_INTERVAL = 100;

    public MessagePersistenceListener(MqttMessageRecordRepository messageRepository,
                                      MqttPersistenceProperties persistenceProperties) {
        this.messageRepository = messageRepository;
        this.persistenceProperties = persistenceProperties;
    }

    @Async
    @EventListener
    @Transactional
    public void handleMonitorEvent(MonitorService.MonitorEvent event) {
        if ("MESSAGE_RECEIVED".equals(event.getType())) {
            Object data = event.getData();
            if (data instanceof MqttMessageRecord record) {
                try {
                    MqttMessageRecordEntity entity = new MqttMessageRecordEntity();
                    entity.setTopic(record.getTopic());
                    entity.setPayload(record.getPayload());
                    entity.setClientId(record.getClientId());
                    entity.setQos(record.getQos());
                    entity.setDirection(record.getDirection());
                    entity.setTimestamp(Instant.ofEpochMilli(record.getTimestamp()));
                    entity.setRetained(false);

                    messageRepository.save(entity);
                    log.debug("消息已持久化: topic={}, clientId={}", record.getTopic(), record.getClientId());

                    // 每隔 CHECK_INTERVAL 条消息检查一次数据量限制
                    int maxRows = persistenceProperties.getMaxRows();
                    if (maxRows > 0 && saveCount.incrementAndGet() % CHECK_INTERVAL == 0) {
                        long count = messageRepository.countAll();
                        if (count > maxRows) {
                            int deleted = messageRepository.deleteOldest(maxRows);
                            log.info("清理超额消息记录: 当前={}, 上限={}, 删除={}", count, maxRows, deleted);
                        }
                    }
                } catch (Exception e) {
                    log.error("消息持久化失败", e);
                }
            }
        }
    }
}
