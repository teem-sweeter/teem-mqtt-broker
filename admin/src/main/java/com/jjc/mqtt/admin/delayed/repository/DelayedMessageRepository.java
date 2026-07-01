package com.jjc.mqtt.admin.delayed.repository;

import com.jjc.mqtt.admin.common.BaseJpaRepository;
import com.jjc.mqtt.admin.delayed.entity.DelayedMessageEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface DelayedMessageRepository extends BaseJpaRepository<DelayedMessageEntity, Long> {

    List<DelayedMessageEntity> findByStatusOrderByScheduledTimeAsc(String status);

    @Query("SELECT d FROM DelayedMessageEntity d WHERE d.status = 'PENDING' AND d.scheduledTime <= :now")
    List<DelayedMessageEntity> findDueMessages(@Param("now") Instant now);

    @Query("SELECT COUNT(d) FROM DelayedMessageEntity d WHERE d.status = :status")
    long countByStatus(@Param("status") String status);
}
