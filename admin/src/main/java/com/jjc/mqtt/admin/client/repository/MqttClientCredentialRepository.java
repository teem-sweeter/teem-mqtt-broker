package com.jjc.mqtt.admin.client.repository;

import com.jjc.mqtt.admin.client.entity.MqttClientCredentialEntity;
import com.jjc.mqtt.admin.common.BaseJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * MQTT 客户端凭证 Repository
 *
 * @author sweeter
 */
@Repository
public interface MqttClientCredentialRepository extends BaseJpaRepository<MqttClientCredentialEntity, Long> {

    Optional<MqttClientCredentialEntity> findByClientId(String clientId);

    Optional<MqttClientCredentialEntity> findByUsername(String username);

    void deleteByClientId(String clientId);
}
