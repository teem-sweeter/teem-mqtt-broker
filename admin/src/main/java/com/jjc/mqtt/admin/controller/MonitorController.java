package com.jjc.mqtt.admin.controller;

import com.jjc.mqtt.monitor.ClientInfo;
import com.jjc.mqtt.monitor.MqttMessageRecord;
import com.jjc.mqtt.monitor.MqttStats;
import com.jjc.mqtt.monitor.MonitorService;
import io.moquette.broker.Server;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.MqttMessageBuilders;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "MQTT监控", description = "MQTT监控")
@RestController
@RequestMapping("/v1/monitor")
public class MonitorController {

    private static final Logger log = LoggerFactory.getLogger(MonitorController.class);

    @Autowired
    private MonitorService monitorService;

    @Autowired(required = false)
    private Server mqttBroker;

    @GetMapping("/stats")
    public ResponseEntity<MqttStats> getStats() {
        return ResponseEntity.ok(monitorService.getStats());
    }

    @GetMapping("/clients")
    public ResponseEntity<List<ClientInfo>> getClients() {
        List<ClientInfo> clients = monitorService.getConnectedClients();
        clients.forEach(client -> monitorService.refreshClientIpPort(client.getClientId()));
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/clients/{clientId}")
    public ResponseEntity<?> getClient(@PathVariable String clientId) {
        return monitorService.getClient(clientId)
                .map(client -> {
                    monitorService.refreshClientIpPort(clientId);
                    return ResponseEntity.ok(client);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/messages")
    public ResponseEntity<List<MqttMessageRecord>> getRecentMessages(
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(monitorService.getRecentMessages(limit));
    }

    @GetMapping("/topics")
    public ResponseEntity<Map<String, Long>> getTopicStats() {
        return ResponseEntity.ok(monitorService.getTopicStats());
    }

    @PostMapping("/clients/{clientId}/disconnect")
    public ResponseEntity<Map<String, Object>> disconnectClient(@PathVariable String clientId) {
        Map<String, Object> result = new HashMap<>();
        boolean success = monitorService.disconnectClient(clientId);
        result.put("success", success);
        result.put("clientId", clientId);
        result.put("message", success ? "断开连接请求已发送" : "客户端不存在");
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/stats")
    public ResponseEntity<Map<String, Object>> clearStats() {
        monitorService.clearStats();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "统计数据已清空");
        return ResponseEntity.ok(result);
    }

    @PostMapping("/publish")
    public ResponseEntity<Map<String, Object>> publishMessage(@RequestBody PublishRequest request) {
        Map<String, Object> result = new HashMap<>();

        if (mqttBroker == null) {
            result.put("success", false);
            result.put("message", "MQTT Broker未初始化");
            return ResponseEntity.ok(result);
        }

        try {
            MqttQoS qos = MqttQoS.valueOf(request.getQos());
            ByteBuf payload = Unpooled.copiedBuffer(request.getPayload(), StandardCharsets.UTF_8);

            MqttPublishMessage publishMessage = MqttMessageBuilders.publish()
                    .topicName(request.getTopic())
                    .qos(qos)
                    .payload(payload)
                    .build();

            mqttBroker.internalPublish(publishMessage, "admin-console");

            result.put("success", true);
            result.put("message", "消息已发布到 " + request.getTopic());
            log.info("管理员发布消息: topic={}, qos={}", request.getTopic(), request.getQos());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "发布失败: " + e.getMessage());
            log.error("消息发布失败", e);
        }

        return ResponseEntity.ok(result);
    }

    public static class PublishRequest {
        private String topic;
        private int qos;
        private String payload;

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public int getQos() {
            return qos;
        }

        public void setQos(int qos) {
            this.qos = qos;
        }

        public String getPayload() {
            return payload;
        }

        public void setPayload(String payload) {
            this.payload = payload;
        }
    }
}