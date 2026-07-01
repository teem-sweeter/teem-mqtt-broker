package com.jjc.mqtt.admin.bridge.controller;

import com.jjc.mqtt.admin.bridge.model.*;
import com.jjc.mqtt.admin.bridge.service.BridgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 桥接链路管理 API
 *
 * @author sweeter
 */
@Tag(name = "桥接管理", description = "MQTT桥接链路与路由规则管理")
@RequestMapping("/v1/bridges")
@RestController
public class BridgeController {

    private final BridgeService bridgeService;

    public BridgeController(BridgeService bridgeService) {
        this.bridgeService = bridgeService;
    }

    @Operation(summary = "获取所有桥接链路")
    @GetMapping
    public ResponseEntity<List<Bridge>> getBridges() {
        return ResponseEntity.ok(bridgeService.findAll());
    }

    @Operation(summary = "获取单个桥接链路详情")
    @GetMapping("/{id}")
    public ResponseEntity<Bridge> getBridge(@PathVariable Long id) {
        return ResponseEntity.ok(bridgeService.findById(id));
    }

    @Operation(summary = "创建桥接链路")
    @PostMapping
    public ResponseEntity<Bridge> createBridge(@Valid @RequestBody Bridge dto) {
        return ResponseEntity.ok(bridgeService.create(dto));
    }

    @Operation(summary = "更新桥接链路")
    @PutMapping("/{id}")
    public ResponseEntity<Bridge> updateBridge(@PathVariable Long id, @Valid @RequestBody Bridge dto) {
        return ResponseEntity.ok(bridgeService.update(id, dto));
    }

    @Operation(summary = "删除桥接链路")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBridge(@PathVariable Long id) {
        bridgeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "启用/禁用桥接链路")
    @PostMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleBridge(
            @PathVariable Long id,
            @RequestParam boolean enable) {
        bridgeService.toggle(id, enable);
        return ResponseEntity.ok(Map.of("success", true, "enabled", enable));
    }

    @Operation(summary = "获取桥接链路实时统计")
    @GetMapping("/{id}/stats")
    public ResponseEntity<Map<String, Object>> getBridgeStats(@PathVariable Long id) {
        return ResponseEntity.ok(bridgeService.getStats(id));
    }

    @Operation(summary = "测试连接")
    @PostMapping("/test-connect")
    public ResponseEntity<TestConnectionResponse> testConnection(@RequestBody TestConnectionRequest req) {
        return ResponseEntity.ok(bridgeService.testConnection(req));
    }


    @Operation(summary = "获取指定桥接的路由规则")
    @GetMapping("/{id}/routes")
    public ResponseEntity<List<RouteRule>> getRouteRules(@PathVariable Long id) {
        return ResponseEntity.ok(bridgeService.getRouteRules(id));
    }

    @Operation(summary = "批量保存路由规则")
    @PutMapping("/{id}/routes")
    public ResponseEntity<List<RouteRule>> saveRouteRules(
            @PathVariable Long id,
            @RequestBody List<RouteRule> rules) {
        return ResponseEntity.ok(bridgeService.saveRouteRules(id, rules));
    }
}
