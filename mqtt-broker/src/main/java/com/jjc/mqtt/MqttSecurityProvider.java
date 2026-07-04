package com.jjc.mqtt;

import java.util.Optional;

/**
 * MQTT 动态安全服务提供者接口 (用于解耦 mqtt-broker 与 admin 模块)
 *
 * @author sweeter
 */
public interface MqttSecurityProvider {

    /**
     * 校验设备用户名和密码
     *
     * @param clientId 客户端ID
     * @param username 用户名
     * @param password 密码字节数组
     * @return Optional.empty() 表示使用静态账号；Optional.of(true/false) 表示动态认证结果
     */
    Optional<Boolean> authenticate(String clientId, String username, byte[] password);

    /**
     * 校验主题访问控制列表 (ACL)
     *
     * @param clientId 客户端ID
     * @param username 用户名
     * @param topic 主题名称
     * @param permission 权限类型 (PUB/SUB)
     * @return 是否允许
     */
    boolean checkAcl(String clientId, String username, String topic, String permission);
}
