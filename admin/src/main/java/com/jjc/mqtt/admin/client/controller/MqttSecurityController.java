package com.jjc.mqtt.admin.client.controller;

import com.jjc.mqtt.admin.client.entity.MqttAclRuleEntity;
import com.jjc.mqtt.admin.client.entity.MqttClientCredentialEntity;
import com.jjc.mqtt.admin.client.service.MqttSecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * MQTT 动态安全控制 API (设备认证凭证与 ACL 规则)
 *
 * @author sweeter
 */
@Tag(name = "安全管理", description = "MQTT 客户端凭证与 ACL 权限访问控制")
@RequestMapping("/v1/security")
@RestController
public class MqttSecurityController {

    private final MqttSecurityService securityService;

    public MqttSecurityController(MqttSecurityService securityService) {
        this.securityService = securityService;
    }

    // ================= 设备凭证管理 (Client Credentials) =================

    @Operation(summary = "获取所有设备凭证列表")
    @GetMapping("/credentials")
    public ResponseEntity<List<MqttClientCredentialEntity>> getAllCredentials() {
        return ResponseEntity.ok(securityService.findAllCredentials());
    }

    @Operation(summary = "获取单个设备凭证详情")
    @GetMapping("/credentials/{id}")
    public ResponseEntity<MqttClientCredentialEntity> getCredentialById(@PathVariable Long id) {
        return ResponseEntity.ok(securityService.findCredentialById(id));
    }

    @Operation(summary = "创建设备凭证")
    @PostMapping("/credentials")
    public ResponseEntity<MqttClientCredentialEntity> createCredential(@Valid @RequestBody MqttClientCredentialEntity entity) {
        return ResponseEntity.ok(securityService.createCredential(entity));
    }

    @Operation(summary = "更新设备凭证")
    @PutMapping("/credentials/{id}")
    public ResponseEntity<MqttClientCredentialEntity> updateCredential(@PathVariable Long id, @Valid @RequestBody MqttClientCredentialEntity entity) {
        return ResponseEntity.ok(securityService.updateCredential(id, entity));
    }

    @Operation(summary = "删除设备凭证")
    @DeleteMapping("/credentials/{id}")
    public ResponseEntity<Void> deleteCredential(@PathVariable Long id) {
        securityService.deleteCredential(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "启用/禁用设备凭证")
    @PostMapping("/credentials/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleCredential(@PathVariable Long id, @RequestParam boolean enable) {
        MqttClientCredentialEntity credential = securityService.findCredentialById(id);
        credential.setEnabled(enable);
        securityService.updateCredential(id, credential);
        return ResponseEntity.ok(Map.of("success", true, "enabled", enable));
    }

    // ================= ACL 规则管理 (ACL Rules) =================

    @Operation(summary = "获取所有 ACL 规则列表")
    @GetMapping("/acl-rules")
    public ResponseEntity<List<MqttAclRuleEntity>> getAllAclRules() {
        return ResponseEntity.ok(securityService.findAllAclRules());
    }

    @Operation(summary = "获取单个 ACL 规则详情")
    @GetMapping("/acl-rules/{id}")
    public ResponseEntity<MqttAclRuleEntity> getAclRuleById(@PathVariable Long id) {
        return ResponseEntity.ok(securityService.findAclRuleById(id));
    }

    @Operation(summary = "创建 ACL 规则")
    @PostMapping("/acl-rules")
    public ResponseEntity<MqttAclRuleEntity> createAclRule(@Valid @RequestBody MqttAclRuleEntity entity) {
        return ResponseEntity.ok(securityService.createAclRule(entity));
    }

    @Operation(summary = "更新 ACL 规则")
    @PutMapping("/acl-rules/{id}")
    public ResponseEntity<MqttAclRuleEntity> updateAclRule(@PathVariable Long id, @Valid @RequestBody MqttAclRuleEntity entity) {
        return ResponseEntity.ok(securityService.updateAclRule(id, entity));
    }

    @Operation(summary = "删除 ACL 规则")
    @DeleteMapping("/acl-rules/{id}")
    public ResponseEntity<Void> deleteAclRule(@PathVariable Long id) {
        securityService.deleteAclRule(id);
        return ResponseEntity.noContent().build();
    }
}
