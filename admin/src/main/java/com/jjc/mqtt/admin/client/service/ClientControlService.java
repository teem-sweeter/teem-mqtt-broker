package com.jjc.mqtt.admin.client.service;

import com.jjc.mqtt.admin.client.entity.ClientRuleEntity;
import com.jjc.mqtt.admin.client.repository.ClientRuleRepository;
import com.jjc.mqtt.monitor.ClientInfo;
import com.jjc.mqtt.monitor.MonitorService;
import io.moquette.broker.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端管控服务
 * <p>
 * 管理客户端的发送/接收权限，踢出客户端。
 * 维护内存缓存供拦截处理器快速查询。
 *
 * @author sweeter
 */
@Service
public class ClientControlService {

    private static final Logger log = LoggerFactory.getLogger(ClientControlService.class);

    private final ClientRuleRepository ruleRepository;
    private final MonitorService monitorService;
    private final Server mqttBroker;

    /** 内存缓存: clientId -> 规则 */
    private final Map<String, ClientRuleEntity> ruleCache = new ConcurrentHashMap<>();

    public ClientControlService(ClientRuleRepository ruleRepository,
                                MonitorService monitorService,
                                Server mqttBroker) {
        this.ruleRepository = ruleRepository;
        this.monitorService = monitorService;
        this.mqttBroker = mqttBroker;
        // 启动时加载缓存
        ruleRepository.findAll().forEach(r -> ruleCache.put(r.getClientId(), r));
    }

    /**
     * 获取所有已连接客户端及其管控状态
     */
    public List<Map<String, Object>> getManagedClients() {
        List<ClientInfo> connected = monitorService.getConnectedClients();
        List<Map<String, Object>> result = new ArrayList<>();

        for (ClientInfo client : connected) {
            // 刷新 IP 和 Session 运行指标
            monitorService.refreshClientMetrics(client.getClientId());

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("clientId", client.getClientId());
            item.put("username", client.getUsername());
            item.put("ipAddress", client.getIpAddress());
            item.put("port", client.getPort());
            item.put("keepAlive", client.getKeepAlive());
            item.put("connectTime", client.getConnectTime());
            item.put("subscriptions", client.getSubscriptions());
            item.put("subscriptionDetails", client.getSubscriptionDetails());
            // 连接信息
            item.put("protocolVersion", client.getProtocolVersion());
            item.put("cleanSession", client.isCleanSession());
            // 遗嘱信息
            item.put("willFlag", client.isWillFlag());
            item.put("willTopic", client.getWillTopic());
            item.put("willMessage", client.getWillMessage());
            item.put("willQos", client.getWillQos());
            item.put("willRetain", client.isWillRetain());
            // 运行指标
            item.put("inflightCount", client.getInflightCount());
            item.put("queuedCount", client.getQueuedCount());

            ClientRuleEntity rule = ruleCache.get(client.getClientId());
            item.put("sendDisabled", rule != null && Boolean.TRUE.equals(rule.getSendDisabled()));
            item.put("receiveDisabled", rule != null && Boolean.TRUE.equals(rule.getReceiveDisabled()));
            item.put("remark", rule != null ? rule.getRemark() : null);

            result.add(item);
        }
        return result;
    }

    /**
     * 设置客户端发送权限
     */
    @Transactional
    public void setSendDisabled(String clientId, boolean disabled) {
        ClientRuleEntity rule = getOrCreateRule(clientId);
        rule.setSendDisabled(disabled);
        ruleRepository.save(rule);
        ruleCache.put(clientId, rule);
        log.info("客户端发送权限更新: clientId={}, sendDisabled={}", clientId, disabled);
    }

    /**
     * 设置客户端接收权限
     */
    @Transactional
    public void setReceiveDisabled(String clientId, boolean disabled) {
        ClientRuleEntity rule = getOrCreateRule(clientId);
        rule.setReceiveDisabled(disabled);
        ruleRepository.save(rule);
        ruleCache.put(clientId, rule);
        log.info("客户端接收权限更新: clientId={}, receiveDisabled={}", clientId, disabled);
    }

    /**
     * 踢出客户端
     */
    public boolean kickClient(String clientId) {
        boolean success = monitorService.disconnectClient(clientId);
        if (success) {
            log.info("已踢出客户端: {}", clientId);
        }
        return success;
    }

    /**
     * 判断客户端是否禁止发送
     */
    public boolean isSendDisabled(String clientId) {
        ClientRuleEntity rule = ruleCache.get(clientId);
        return rule != null && Boolean.TRUE.equals(rule.getSendDisabled());
    }

    /**
     * 判断客户端是否禁止接收
     */
    public boolean isReceiveDisabled(String clientId) {
        ClientRuleEntity rule = ruleCache.get(clientId);
        return rule != null && Boolean.TRUE.equals(rule.getReceiveDisabled());
    }

    /**
     * 获取或创建规则
     */
    private ClientRuleEntity getOrCreateRule(String clientId) {
        return ruleCache.computeIfAbsent(clientId, id -> {
            return ruleRepository.findByClientId(id).orElseGet(() -> {
                ClientRuleEntity r = new ClientRuleEntity();
                r.setClientId(id);
                r.setSendDisabled(false);
                r.setReceiveDisabled(false);
                return r;
            });
        });
    }
}
