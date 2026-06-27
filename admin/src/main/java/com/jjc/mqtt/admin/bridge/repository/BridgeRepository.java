package com.jjc.mqtt.admin.bridge.repository;

import com.jjc.mqtt.admin.bridge.entity.BridgeEntity;
import com.jjc.mqtt.admin.common.BaseJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 桥接链路 Repository
 *
 * @author sweeter
 */
@Repository
public interface BridgeRepository extends BaseJpaRepository<BridgeEntity, Long> {

    List<BridgeEntity> findByEnabledTrue();
}
