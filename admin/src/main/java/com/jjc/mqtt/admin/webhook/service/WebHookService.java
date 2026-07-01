package com.jjc.mqtt.admin.webhook.service;

import com.jjc.mqtt.admin.webhook.entity.WebHookEntity;
import com.jjc.mqtt.admin.webhook.repository.WebHookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WebHookService {

    private static final Logger log = LoggerFactory.getLogger(WebHookService.class);

    private final WebHookRepository webHookRepository;

    public WebHookService(WebHookRepository webHookRepository) {
        this.webHookRepository = webHookRepository;
    }

    public List<WebHookEntity> findAll() {
        return webHookRepository.findAll();
    }

    public WebHookEntity findById(Long id) {
        return webHookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("WebHook not found: " + id));
    }

    public WebHookEntity create(WebHookEntity entity) {
        return webHookRepository.save(entity);
    }

    public WebHookEntity update(Long id, WebHookEntity updated) {
        WebHookEntity existing = findById(id);
        existing.setName(updated.getName());
        existing.setUrl(updated.getUrl());
        existing.setTopicFilter(updated.getTopicFilter());
        existing.setQos(updated.getQos());
        existing.setHeaders(updated.getHeaders());
        existing.setContentType(updated.getContentType());
        existing.setRetryCount(updated.getRetryCount());
        existing.setRetryInterval(updated.getRetryInterval());
        existing.setConnectTimeout(updated.getConnectTimeout());
        existing.setReadTimeout(updated.getReadTimeout());
        existing.setEnabled(updated.getEnabled());
        existing.setRemark(updated.getRemark());
        return webHookRepository.save(existing);
    }

    public void delete(Long id) {
        WebHookEntity entity = findById(id);
        webHookRepository.delete(entity);
    }

    public WebHookEntity toggle(Long id, boolean enable) {
        WebHookEntity entity = findById(id);
        entity.setEnabled(enable);
        return webHookRepository.save(entity);
    }

    public List<WebHookEntity> getActiveWebhooks() {
        return webHookRepository.findByEnabledTrue();
    }
}
