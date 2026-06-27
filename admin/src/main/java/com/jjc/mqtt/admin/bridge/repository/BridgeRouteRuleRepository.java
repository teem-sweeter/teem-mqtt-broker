package com.jjc.mqtt.admin.bridge.repository;

import com.jjc.mqtt.admin.bridge.entity.BridgeRouteRuleEntity;
import com.jjc.mqtt.admin.common.BaseJpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 桥接路由规则 Repository
 *
 * @author sweeter
 */
@Repository
public interface BridgeRouteRuleRepository extends BaseJpaRepository<BridgeRouteRuleEntity, Long> {

    List<BridgeRouteRuleEntity> findByBridgeIdOrderBySortOrderAsc(Long bridgeId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM BridgeRouteRuleEntity r WHERE r.bridgeId = :bridgeId")
    int deleteByBridgeId(@Param("bridgeId") Long bridgeId);

    @Query("SELECT COUNT(r) FROM BridgeRouteRuleEntity r WHERE r.bridgeId = :bridgeId")
    long countByBridgeId(@Param("bridgeId") Long bridgeId);
}
