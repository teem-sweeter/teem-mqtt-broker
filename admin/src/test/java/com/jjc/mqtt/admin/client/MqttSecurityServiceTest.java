package com.jjc.mqtt.admin.client;

import com.jjc.mqtt.admin.client.entity.MqttAclRuleEntity;
import com.jjc.mqtt.admin.client.entity.MqttClientCredentialEntity;
import com.jjc.mqtt.admin.client.repository.MqttAclRuleRepository;
import com.jjc.mqtt.admin.client.repository.MqttClientCredentialRepository;
import com.jjc.mqtt.admin.client.service.MqttSecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MqttSecurityServiceTest {

    @Autowired
    private MqttSecurityService securityService;

    @Autowired
    private MqttClientCredentialRepository credentialRepository;

    @Autowired
    private MqttAclRuleRepository aclRuleRepository;

    @BeforeEach
    void setUp() {
        credentialRepository.deleteAll();
        aclRuleRepository.deleteAll();
        securityService.invalidateCache();
    }

    @Test
    void testAuthenticate() {
        // 1. 测试未录入设备，应返回 Optional.empty()
        Optional<Boolean> resultEmpty = securityService.authenticate("test_client", "test_user", "pass".getBytes(StandardCharsets.UTF_8));
        assertTrue(resultEmpty.isEmpty());

        // 2. 录入设备并测试密码匹配
        MqttClientCredentialEntity cred = new MqttClientCredentialEntity();
        cred.setClientId("test_client");
        cred.setUsername("test_user");
        cred.setPassword("password123");
        cred.setEnabled(true);
        securityService.createCredential(cred);

        // 正确密码
        Optional<Boolean> resultOk = securityService.authenticate("test_client", "test_user", "password123".getBytes(StandardCharsets.UTF_8));
        assertTrue(resultOk.isPresent());
        assertTrue(resultOk.get());

        // 错误密码
        Optional<Boolean> resultErr = securityService.authenticate("test_client", "test_user", "wrong_pass".getBytes(StandardCharsets.UTF_8));
        assertTrue(resultErr.isPresent());
        assertFalse(resultErr.get());

        // 禁用状态测试
        cred.setEnabled(false);
        securityService.updateCredential(cred.getId(), cred);
        Optional<Boolean> resultDisabled = securityService.authenticate("test_client", "test_user", "password123".getBytes(StandardCharsets.UTF_8));
        assertTrue(resultDisabled.isPresent());
        assertFalse(resultDisabled.get());
    }

    @Test
    void testCheckAcl() {
        // 1. 无任何规则时，默认应允许
        boolean allowedDefault = securityService.checkAcl("client1", "user1", "sensor/1/temp", "PUB");
        assertTrue(allowedDefault);

        // 2. 添加只允许发布 sensor/+/temp 的规则
        MqttAclRuleEntity rule1 = new MqttAclRuleEntity();
        rule1.setClientId("client1");
        rule1.setTopic("sensor/+/temp");
        rule1.setPermission("PUB");
        rule1.setAction("ALLOW");
        rule1.setPriority(10);
        securityService.createAclRule(rule1);

        // 添加拒绝所有发布订阅的规则，优先级较低
        MqttAclRuleEntity rule2 = new MqttAclRuleEntity();
        rule2.setClientId("*");
        rule2.setTopic("#");
        rule2.setPermission("PUB_SUB");
        rule2.setAction("DENY");
        rule2.setPriority(1);
        securityService.createAclRule(rule2);

        // client1 发布 sensor/1/temp -> 允许
        assertTrue(securityService.checkAcl("client1", "user1", "sensor/1/temp", "PUB"));

        // client1 订阅 sensor/1/temp -> 拒绝
        assertFalse(securityService.checkAcl("client1", "user1", "sensor/1/temp", "SUB"));

        // client2 发布 sensor/1/temp -> 拒绝
        assertFalse(securityService.checkAcl("client2", "user2", "sensor/1/temp", "PUB"));
    }

    @Test
    void testCheckAclPlaceholders() {
        // 添加规则：允许订阅自己 client ID 专属的主题
        MqttAclRuleEntity rule = new MqttAclRuleEntity();
        rule.setClientId("*");
        rule.setTopic("device/${clientId}/#");
        rule.setPermission("PUB_SUB");
        rule.setAction("ALLOW");
        rule.setPriority(100);
        securityService.createAclRule(rule);

        // 默认拒绝所有其他
        MqttAclRuleEntity denyAll = new MqttAclRuleEntity();
        denyAll.setClientId("*");
        denyAll.setTopic("#");
        denyAll.setPermission("PUB_SUB");
        denyAll.setAction("DENY");
        denyAll.setPriority(1);
        securityService.createAclRule(denyAll);

        // device_01 访问 device/device_01/temp -> 允许
        assertTrue(securityService.checkAcl("device_01", "user", "device/device_01/temp", "PUB"));

        // device_01 访问 device/device_02/temp -> 拒绝
        assertFalse(securityService.checkAcl("device_01", "user", "device/device_02/temp", "PUB"));
    }

    @Test
    void testMatchTopic() {
        assertTrue(MqttSecurityService.matchTopic("sensor/1/temp", "sensor/+/temp"));
        assertTrue(MqttSecurityService.matchTopic("sensor/1/temp/value", "sensor/#"));
        assertFalse(MqttSecurityService.matchTopic("sensor/1/temp/value", "sensor/+/temp"));
    }
}
