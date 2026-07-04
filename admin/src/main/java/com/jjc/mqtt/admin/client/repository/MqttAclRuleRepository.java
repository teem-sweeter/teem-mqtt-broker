package com.jjc.mqtt.admin.client.repository;

import com.jjc.mqtt.admin.client.entity.MqttAclRuleEntity;
import com.jjc.mqtt.admin.common.BaseJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MQTT ACL 规则 Repository
 *
 * @author sweeter
 */
@Repository
public interface MqttAclRuleRepository extends BaseJpaRepository<MqttAclRuleEntity, Long> {

    List<MqttAclRuleEntity> findByClientIdOrderByPriorityDesc(String clientId);

    List<MqttAclRuleEntity> findByUsernameOrderByPriorityDesc(String username);

    List<MqttAclRuleEntity> findAllByOrderByPriorityDesc();
}
