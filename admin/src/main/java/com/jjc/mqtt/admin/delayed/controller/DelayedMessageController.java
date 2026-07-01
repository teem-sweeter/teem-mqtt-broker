package com.jjc.mqtt.admin.delayed.controller;

import com.jjc.mqtt.admin.delayed.entity.DelayedMessageEntity;
import com.jjc.mqtt.admin.delayed.service.DelayedMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "延迟消息", description = "延迟消息管理")
@RestController
@RequestMapping("/v1/delayed-messages")
public class DelayedMessageController {

    private final DelayedMessageService delayedMessageService;

    public DelayedMessageController(DelayedMessageService delayedMessageService) {
        this.delayedMessageService = delayedMessageService;
    }

    @Operation(summary = "创建延迟消息")
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody CreateRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            DelayedMessageEntity entity = delayedMessageService.schedule(
                    request.getTopic(), request.getQos(), request.getPayload(),
                    request.isRetain(), request.getDelaySeconds());
            result.put("success", true);
            result.put("id", entity.getId());
            result.put("scheduledTime", entity.getScheduledTime().toString());
            result.put("message", "延迟消息已创建，将在" + request.getDelaySeconds() + "秒后投递");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "创建失败: " + e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "取消延迟消息")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> cancel(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        boolean success = delayedMessageService.cancel(id);
        result.put("success", success);
        result.put("message", success ? "延迟消息已取消" : "消息不存在或已投递");
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "获取待投递消息列表")
    @GetMapping
    public ResponseEntity<List<DelayedMessageEntity>> list(
            @RequestParam(defaultValue = "PENDING") String status) {
        if ("ALL".equals(status)) {
            return ResponseEntity.ok(delayedMessageService.listAll());
        }
        return ResponseEntity.ok(delayedMessageService.listPending());
    }

    @Operation(summary = "获取延迟消息统计")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> stats() {
        return ResponseEntity.ok(delayedMessageService.getStats());
    }

    public static class CreateRequest {
        @NotBlank(message = "主题不能为空")
        private String topic;
        private int qos;
        @NotBlank(message = "消息内容不能为空")
        private String payload;
        private boolean retain = false;
        @Min(value = 1, message = "延迟时间至少1秒")
        private int delaySeconds;

        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
        public int getQos() { return qos; }
        public void setQos(int qos) { this.qos = qos; }
        public String getPayload() { return payload; }
        public void setPayload(String payload) { this.payload = payload; }
        public boolean isRetain() { return retain; }
        public void setRetain(boolean retain) { this.retain = retain; }
        public int getDelaySeconds() { return delaySeconds; }
        public void setDelaySeconds(int delaySeconds) { this.delaySeconds = delaySeconds; }
    }
}
