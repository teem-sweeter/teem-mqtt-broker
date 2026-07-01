package com.jjc.mqtt.admin.webhook.repository;

import com.jjc.mqtt.admin.common.BaseJpaRepository;
import com.jjc.mqtt.admin.webhook.entity.WebHookEntity;

import java.util.List;

public interface WebHookRepository extends BaseJpaRepository<WebHookEntity, Long> {

    List<WebHookEntity> findByEnabledTrue();
}
