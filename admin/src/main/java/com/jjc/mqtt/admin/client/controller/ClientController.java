package com.jjc.mqtt.admin.client.controller;

import com.jjc.mqtt.admin.client.service.ClientControlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 客户端管理 API
 *
 * @author sweeter
 */
@Tag(name = "客户端管理", description = "MQTT客户端管控：踢出、发送/接收权限控制")
@RequestMapping("/v1/clients")
@RestController
public class ClientController {

    private final ClientControlService clientControlService;

    public ClientController(ClientControlService clientControlService) {
        this.clientControlService = clientControlService;
    }

    @Operation(summary = "获取已连接客户端列表（含管控状态）")
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getClients() {
        return ResponseEntity.ok(clientControlService.getManagedClients());
    }

    @Operation(summary = "踢出客户端")
    @PostMapping("/{clientId}/kick")
    public ResponseEntity<Map<String, Object>> kickClient(@PathVariable String clientId) {
        boolean success = clientControlService.kickClient(clientId);
        return ResponseEntity.ok(Map.of(
                "success", success,
                "clientId", clientId,
                "message", success ? "已踢出" : "客户端不存在"
        ));
    }

    @Operation(summary = "设置客户端发送权限")
    @PostMapping("/{clientId}/send")
    public ResponseEntity<Map<String, Object>> setSendDisabled(
            @PathVariable String clientId,
            @RequestParam boolean disabled) {
        clientControlService.setSendDisabled(clientId, disabled);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "clientId", clientId,
                "sendDisabled", disabled
        ));
    }

    @Operation(summary = "设置客户端接收权限")
    @PostMapping("/{clientId}/receive")
    public ResponseEntity<Map<String, Object>> setReceiveDisabled(
            @PathVariable String clientId,
            @RequestParam boolean disabled) {
        clientControlService.setReceiveDisabled(clientId, disabled);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "clientId", clientId,
                "receiveDisabled", disabled
        ));
    }
}
