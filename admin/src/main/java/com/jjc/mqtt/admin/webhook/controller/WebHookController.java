package com.jjc.mqtt.admin.webhook.controller;

import com.jjc.mqtt.admin.webhook.entity.WebHookEntity;
import com.jjc.mqtt.admin.webhook.service.WebHookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "WebHook 管理", description = "MQTT 消息 HTTP 转发配置")
@RequestMapping("/v1/webhooks")
@RestController
public class WebHookController {

    private final WebHookService webHookService;

    public WebHookController(WebHookService webHookService) {
        this.webHookService = webHookService;
    }

    @Operation(summary = "获取所有 WebHook")
    @GetMapping
    public ResponseEntity<List<WebHookEntity>> getAll() {
        return ResponseEntity.ok(webHookService.findAll());
    }

    @Operation(summary = "获取单个 WebHook 详情")
    @GetMapping("/{id}")
    public ResponseEntity<WebHookEntity> getById(@PathVariable Long id) {
        return ResponseEntity.ok(webHookService.findById(id));
    }

    @Operation(summary = "创建 WebHook")
    @PostMapping
    public ResponseEntity<WebHookEntity> create(@Valid @RequestBody WebHookEntity entity) {
        return ResponseEntity.ok(webHookService.create(entity));
    }

    @Operation(summary = "更新 WebHook")
    @PutMapping("/{id}")
    public ResponseEntity<WebHookEntity> update(@PathVariable Long id, @Valid @RequestBody WebHookEntity entity) {
        return ResponseEntity.ok(webHookService.update(id, entity));
    }

    @Operation(summary = "删除 WebHook")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        webHookService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "启用/禁用 WebHook")
    @PostMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggle(@PathVariable Long id, @RequestParam boolean enable) {
        webHookService.toggle(id, enable);
        return ResponseEntity.ok(Map.of("success", true, "enabled", enable));
    }
}
