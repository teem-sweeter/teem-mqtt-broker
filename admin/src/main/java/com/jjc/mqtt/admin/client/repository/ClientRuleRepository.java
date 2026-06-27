package com.jjc.mqtt.admin.client.repository;

import com.jjc.mqtt.admin.client.entity.ClientRuleEntity;
import com.jjc.mqtt.admin.common.BaseJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 客户端管控规则 Repository
 *
 * @author sweeter
 */
@Repository
public interface ClientRuleRepository extends BaseJpaRepository<ClientRuleEntity, Long> {

    Optional<ClientRuleEntity> findByClientId(String clientId);

    void deleteByClientId(String clientId);
}
