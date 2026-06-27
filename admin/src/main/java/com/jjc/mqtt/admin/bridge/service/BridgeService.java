package com.jjc.mqtt.admin.bridge.service;

import com.jjc.mqtt.admin.bridge.model.*;
import com.jjc.mqtt.admin.bridge.entity.BridgeEntity;
import com.jjc.mqtt.admin.bridge.entity.BridgeRouteRuleEntity;
import com.jjc.mqtt.admin.bridge.repository.BridgeRepository;
import com.jjc.mqtt.admin.bridge.repository.BridgeRouteRuleRepository;
import com.jjc.mqtt.bridge.BridgeEngine;
import com.jjc.mqtt.handler.BridgeInterceptHandler;
import io.moquette.broker.Server;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 桥接链路服务
 * <p>
 * 负责桥接链路的 CRUD、运行时生命周期管理，以及与 BridgeEngine 的交互。
 *
 * @author sweeter
 */
@Service
public class BridgeService {

    private static final Logger log = LoggerFactory.getLogger(BridgeService.class);

    private final BridgeRepository bridgeRepository;
    private final BridgeRouteRuleRepository routeRuleRepository;
    private final Server localBroker;

    /** 运行中的桥接引擎实例: bridgeId -> BridgeEngine */
    private final Map<Long, BridgeEngine> runningEngines = new ConcurrentHashMap<>();

    public BridgeService(BridgeRepository bridgeRepository,
                         BridgeRouteRuleRepository routeRuleRepository,
                         Server localBroker) {
        this.bridgeRepository = bridgeRepository;
        this.routeRuleRepository = routeRuleRepository;
        this.localBroker = localBroker;
    }

    /**
     * 应用启动完成后，自动启动已启用的桥接链路
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        List<BridgeEntity> enabledBridges = bridgeRepository.findByEnabledTrue();
        for (BridgeEntity entity : enabledBridges) {
            try {
                startEngine(entity);
                log.info("自动启动桥接链路: id={}, name={}", entity.getId(), entity.getName());
            } catch (Exception e) {
                log.error("自动启动桥接链路失败: id={}, name={}", entity.getId(), entity.getName(), e);
            }
        }
    }

    // ===== CRUD =====

    /**
     * 获取所有桥接链路（含运行时状态）
     */
    public List<Bridge> findAll() {
        List<BridgeEntity> entities = bridgeRepository.findAll();
        return entities.stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * 获取单个桥接链路
     */
    public Bridge findById(Long id) {
        BridgeEntity entity = bridgeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("桥接链路不存在: " + id));
        Bridge dto = toDTO(entity);
        dto.setRouteRules(getRouteRules(id));
        return dto;
    }

    /**
     * 创建桥接链路
     */
    @Transactional
    public Bridge create(Bridge dto) {
        BridgeEntity entity = new BridgeEntity();
        applyDTO(entity, dto);
        entity = bridgeRepository.save(entity);

        // 保存路由规则
        if (dto.getRouteRules() != null && !dto.getRouteRules().isEmpty()) {
            saveRouteRules(entity.getId(), dto.getRouteRules());
        }

        // 如果启用则启动引擎
        if (Boolean.TRUE.equals(entity.getEnabled())) {
            try {
                startEngine(entity);
            } catch (Exception e) {
                log.error("启动桥接引擎失败: id={}", entity.getId(), e);
            }
        }

        return findById(entity.getId());
    }

    /**
     * 更新桥接链路
     */
    @Transactional
    public Bridge update(Long id, Bridge dto) {
        BridgeEntity entity = bridgeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("桥接链路不存在: " + id));

        applyDTO(entity, dto);
        entity = bridgeRepository.save(entity);

        // 保存成功后再停止旧引擎并重启
        stopEngine(id);
        if (Boolean.TRUE.equals(entity.getEnabled())) {
            try {
                startEngine(entity);
            } catch (Exception e) {
                log.error("重启桥接引擎失败: id={}", id, e);
            }
        }

        return findById(id);
    }

    /**
     * 删除桥接链路
     */
    @Transactional
    public void delete(Long id) {
        BridgeEntity entity = bridgeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("桥接链路不存在: " + id));
        if (Boolean.TRUE.equals(entity.getEnabled())) {
            throw new RuntimeException("请先停用桥接链路再删除");
        }
        stopEngine(id);
        routeRuleRepository.deleteByBridgeId(id);
        bridgeRepository.deleteById(id);
    }

    /**
     * 启用/禁用桥接链路
     */
    @Transactional
    public void toggle(Long id, boolean enable) {
        BridgeEntity entity = bridgeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("桥接链路不存在: " + id));
        entity.setEnabled(enable);
        bridgeRepository.save(entity);

        if (enable) {
            try {
                startEngine(entity);
            } catch (Exception e) {
                log.error("启动桥接引擎失败: id={}", id, e);
                throw new RuntimeException("启动失败: " + e.getMessage());
            }
        } else {
            stopEngine(id);
        }
    }

    // ===== 路由规则 =====

    /**
     * 获取指定桥接的路由规则
     */
    public List<RouteRule> getRouteRules(Long bridgeId) {
        return routeRuleRepository.findByBridgeIdOrderBySortOrderAsc(bridgeId).stream()
                .map(this::toRouteRuleDTO)
                .collect(Collectors.toList());
    }

    /**
     * 批量保存路由规则（覆盖更新）
     */
    @Transactional
    public List<RouteRule> saveRouteRules(Long bridgeId, List<RouteRule> rules) {
        routeRuleRepository.deleteByBridgeId(bridgeId);

        List<BridgeRouteRuleEntity> entities = new ArrayList<>();
        for (int i = 0; i < rules.size(); i++) {
            RouteRule dto = rules.get(i);
            BridgeRouteRuleEntity entity = new BridgeRouteRuleEntity();
            entity.setBridgeId(bridgeId);
            entity.setDirection(dto.getDirection());
            entity.setSourceTopic(dto.getSourceTopic());
            entity.setDestTopic(dto.getDestTopic());
            entity.setQos(dto.getQos() != null ? dto.getQos() : -1);
            entity.setRetainHandling(dto.getRetainHandling() != null ? dto.getRetainHandling() : "keep");
            entity.setSortOrder(i);
            entities.add(entity);
        }
        entities = routeRuleRepository.saveAll(entities);

        // 热更新运行中的引擎
        BridgeEngine engine = runningEngines.get(bridgeId);
        if (engine != null) {
            List<com.jjc.mqtt.bridge.route.RouteRule> routeRules = entities.stream()
                    .map(this::toRouteRule)
                    .collect(Collectors.toList());
            engine.updateRouteRules(routeRules);
        }

        return entities.stream().map(this::toRouteRuleDTO).collect(Collectors.toList());
    }

    // ===== 统计 =====

    /**
     * 获取桥接链路实时统计
     */
    public Map<String, Object> getStats(Long bridgeId) {
        BridgeEntity entity = bridgeRepository.findById(bridgeId)
                .orElseThrow(() -> new RuntimeException("桥接链路不存在: " + bridgeId));

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("id", entity.getId());
        stats.put("name", entity.getName());

        BridgeEngine engine = runningEngines.get(bridgeId);
        if (engine != null) {
            stats.put("status", engine.getStatus());
            stats.put("sentCount", engine.getSentCount());
            stats.put("receivedCount", engine.getReceivedCount());
            stats.put("sentBytes", engine.getSentBytes());
            stats.put("receivedBytes", engine.getReceivedBytes());
            stats.put("lastConnectedTime", engine.getLastConnectedTime());
            stats.put("lastError", engine.getLastError());
        } else {
            stats.put("status", entity.getEnabled() ? "STOPPED" : "DISABLED");
            stats.put("sentCount", 0L);
            stats.put("receivedCount", 0L);
            stats.put("sentBytes", 0L);
            stats.put("receivedBytes", 0L);
            stats.put("lastConnectedTime", null);
            stats.put("lastError", null);
        }
        return stats;
    }

    // ===== 测试连接 =====

    /**
     * 测试与远程 Broker 的连接
     */
    public TestConnectionResponse testConnection(TestConnectionRequest req) {
        MqttAsyncClient client = null;
        try {
            String testClientId = req.getClientId() != null && !req.getClientId().isEmpty()
                    ? req.getClientId()
                    : "bridge-test-" + UUID.randomUUID().toString().substring(0, 8);

            client = new MqttAsyncClient(req.getRemoteUrl(), testClientId);

            MqttConnectionOptions opts = new MqttConnectionOptions();
            opts.setConnectionTimeout(req.getConnectionTimeout() != null ? req.getConnectionTimeout() : 10);
            opts.setCleanStart(true);
            opts.setAutomaticReconnect(false);

            if ("password".equals(req.getAuthType())) {
                if (req.getUsername() != null) opts.setUserName(req.getUsername());
                if (req.getPassword() != null) opts.setPassword(req.getPassword().getBytes(StandardCharsets.UTF_8));
            }

            long start = System.currentTimeMillis();
            // 同步连接
            client.connect(opts).waitForCompletion(10000);
            long latency = System.currentTimeMillis() - start;

            client.disconnect();
            return new TestConnectionResponse(true, "连接成功", latency);

        } catch (MqttException e) {
            return new TestConnectionResponse(false, "连接失败: " + e.getMessage(), null);
        } catch (Exception e) {
            return new TestConnectionResponse(false, "错误: " + e.getMessage(), null);
        } finally {
            if (client != null) {
                try {
                    if (client.isConnected()) client.disconnect();
                    client.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    // ===== 引擎管理 =====

    private void startEngine(BridgeEntity entity) {
        String bridgeIdStr = String.valueOf(entity.getId());
        String clientId = entity.getClientId();
        if (clientId == null || clientId.isEmpty()) {
            clientId = "bridge-" + bridgeIdStr;
        }

        BridgeEngine engine = new BridgeEngine(
                bridgeIdStr,
                entity.getName(),
                entity.getRemoteUrl(),
                clientId,
                entity.getKeepAlive(),
                entity.getConnectionTimeout(),
                entity.getUsername(),
                entity.getPassword(),
                localBroker
        );

        // 加载路由规则
        List<com.jjc.mqtt.bridge.route.RouteRule> rules = routeRuleRepository.findByBridgeIdOrderBySortOrderAsc(entity.getId()).stream()
                .map(this::toRouteRule)
                .collect(Collectors.toList());
        engine.updateRouteRules(rules);

        // 注册到拦截器
        BridgeInterceptHandler.registerEngine(bridgeIdStr, engine);
        runningEngines.put(entity.getId(), engine);
        engine.start();
    }

    private void stopEngine(Long bridgeId) {
        BridgeEngine engine = runningEngines.remove(bridgeId);
        if (engine != null) {
            engine.stop();
            BridgeInterceptHandler.unregisterEngine(String.valueOf(bridgeId));
        }
    }

    // ===== 转换方法 =====

    private Bridge toDTO(BridgeEntity entity) {
        Bridge dto = new Bridge();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setRemoteUrl(entity.getRemoteUrl());
        dto.setClientId(entity.getClientId());
        dto.setKeepAlive(entity.getKeepAlive());
        dto.setConnectionTimeout(entity.getConnectionTimeout());
        dto.setDefaultQos(entity.getDefaultQos());
        dto.setAuthType(entity.getAuthType());
        dto.setUsername(entity.getUsername());
        dto.setEnabled(entity.getEnabled());
        dto.setRemark(entity.getRemark());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        // 运行时状态
        BridgeEngine engine = runningEngines.get(entity.getId());
        if (engine != null) {
            dto.setStatus(engine.getStatus());
            dto.setSentCount(engine.getSentCount());
            dto.setReceivedCount(engine.getReceivedCount());
            dto.setSentBytes(engine.getSentBytes());
            dto.setReceivedBytes(engine.getReceivedBytes());
            dto.setLastConnectedTime(engine.getLastConnectedTime());
            dto.setLastError(engine.getLastError());
        } else {
            dto.setStatus(entity.getEnabled() ? "STOPPED" : "DISABLED");
            dto.setSentCount(0L);
            dto.setReceivedCount(0L);
            dto.setSentBytes(0L);
            dto.setReceivedBytes(0L);
        }

        // 路由规则数量
        dto.setRouteRuleCount((int) routeRuleRepository.countByBridgeId(entity.getId()));

        return dto;
    }

    private void applyDTO(BridgeEntity entity, Bridge dto) {
        entity.setName(dto.getName());
        entity.setRemoteUrl(dto.getRemoteUrl());
        // clientId: 允许为空，保存时自动生成默认值
        String cid = dto.getClientId();
        entity.setClientId((cid != null && !cid.isEmpty()) ? cid : "bridge-" + UUID.randomUUID().toString().substring(0, 8));
        entity.setKeepAlive(dto.getKeepAlive() != null ? dto.getKeepAlive() : 60);
        entity.setConnectionTimeout(dto.getConnectionTimeout() != null ? dto.getConnectionTimeout() : 10);
        entity.setDefaultQos(dto.getDefaultQos() != null ? dto.getDefaultQos() : 0);
        entity.setAuthType(dto.getAuthType() != null ? dto.getAuthType() : "anonymous");
        entity.setUsername(dto.getUsername());
        // 密码和证书：仅在前端显式传入时更新，避免编辑时覆盖已有值
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            entity.setPassword(dto.getPassword());
        }
        if (dto.getCaCert() != null && !dto.getCaCert().isEmpty()) {
            entity.setCaCert(dto.getCaCert());
        }
        if (dto.getClientCert() != null && !dto.getClientCert().isEmpty()) {
            entity.setClientCert(dto.getClientCert());
        }
        if (dto.getClientKey() != null && !dto.getClientKey().isEmpty()) {
            entity.setClientKey(dto.getClientKey());
        }
        entity.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);
        entity.setRemark(dto.getRemark());
    }

    private RouteRule toRouteRuleDTO(BridgeRouteRuleEntity entity) {
        RouteRule dto = new RouteRule();
        dto.setId(entity.getId());
        dto.setDirection(entity.getDirection());
        dto.setSourceTopic(entity.getSourceTopic());
        dto.setDestTopic(entity.getDestTopic());
        dto.setQos(entity.getQos());
        dto.setRetainHandling(entity.getRetainHandling());
        dto.setSortOrder(entity.getSortOrder());
        return dto;
    }

    private com.jjc.mqtt.bridge.route.RouteRule toRouteRule(BridgeRouteRuleEntity entity) {
        com.jjc.mqtt.bridge.route.RouteRule rule = new com.jjc.mqtt.bridge.route.RouteRule();
        rule.setId(entity.getId());
        rule.setDirection(entity.getDirection());
        rule.setSourceTopic(entity.getSourceTopic());
        rule.setDestTopic(entity.getDestTopic());
        rule.setQos(entity.getQos());
        rule.setRetainHandling(entity.getRetainHandling());
        return rule;
    }
}
