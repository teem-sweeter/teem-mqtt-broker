package com.jjc.mqtt.admin.persistence.repository;

import com.jjc.mqtt.admin.common.BaseJpaRepository;
import com.jjc.mqtt.admin.persistence.entity.MqttMessageRecordEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface MqttMessageRecordRepository extends BaseJpaRepository<MqttMessageRecordEntity, Long> {

    @Modifying
    @Query("DELETE FROM MqttMessageRecordEntity m WHERE m.timestamp < :cutoff")
    int deleteByTimestampBefore(@Param("cutoff") Instant cutoff);

    @Query("SELECT COUNT(m) FROM MqttMessageRecordEntity m")
    long countAll();

    @Modifying
    @Query(value = "DELETE FROM mqtt_message_record WHERE id NOT IN (SELECT id FROM mqtt_message_record ORDER BY timestamp DESC LIMIT :keepRows)", nativeQuery = true)
    int deleteOldest(@Param("keepRows") int keepRows);
}
