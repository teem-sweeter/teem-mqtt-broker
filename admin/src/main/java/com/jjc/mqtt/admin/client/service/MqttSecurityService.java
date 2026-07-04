package com.jjc.mqtt.admin.client.service;

import com.jjc.mqtt.PasswordEncoder;
import com.jjc.mqtt.MqttSecurityProvider;
import com.jjc.mqtt.admin.client.entity.MqttAclRuleEntity;
import com.jjc.mqtt.admin.client.entity.MqttClientCredentialEntity;
import com.jjc.mqtt.admin.client.repository.MqttAclRuleRepository;
import com.jjc.mqtt.admin.client.repository.MqttClientCredentialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MQTT 设备认证与 ACL 安全控制服务
 *
 * @author sweeter
 */
@Service
public class MqttSecurityService implements MqttSecurityProvider {

    private static final Logger log = LoggerFactory.getLogger(MqttSecurityService.class);

    private final MqttClientCredentialRepository credentialRepository;
    private final MqttAclRuleRepository aclRuleRepository;

    /** 默认 ACL 规则：ALLOW (允许) 或 DENY (拒绝) */
    @Value("${mqtt.broker.security.default-acl-action:ALLOW}")
    private String defaultAclAction;

    /** 是否开启动态认证：默认为 true */
    @Value("${mqtt.broker.security.enable-dynamic-auth:true}")
    private boolean enableDynamicAuth;

    /** 内存缓存，保存已算出的 (clientId:username:topic:permission) -> Boolean 鉴权结果 */
    private final Map<String, Boolean> aclCache = new ConcurrentHashMap<>();

    public MqttSecurityService(MqttClientCredentialRepository credentialRepository,
                               MqttAclRuleRepository aclRuleRepository) {
        this.credentialRepository = credentialRepository;
        this.aclRuleRepository = aclRuleRepository;
    }

    /**
     * 校验设备用户名和密码
     *
     * @param clientId 客户端ID
     * @param username 用户名
     * @param password 密码字节数组
     * @return 鉴权结果：Optional.empty() 表示数据库中没有匹配的凭证配置（交给静态配置处理）；
     *         Optional.of(true) 表示校验通过；Optional.of(false) 表示被禁用或密码错误
     */
    public Optional<Boolean> authenticate(String clientId, String username, byte[] password) {
        if (!enableDynamicAuth) {
            return Optional.empty();
        }

        // 优先根据 clientId 查询，找不到则按 username 查询
        Optional<MqttClientCredentialEntity> credentialOpt = credentialRepository.findByClientId(clientId);
        if (credentialOpt.isEmpty() && username != null && !username.isEmpty()) {
            credentialOpt = credentialRepository.findByUsername(username);
        }

        if (credentialOpt.isEmpty()) {
            // 数据库中无此设备配置，返回 empty 交给静态账号校验
            return Optional.empty();
        }

        MqttClientCredentialEntity credential = credentialOpt.get();
        if (Boolean.FALSE.equals(credential.getEnabled())) {
            log.warn("设备已被禁用: clientId={}, username={}", clientId, username);
            return Optional.of(false);
        }

        String rawPassword = password == null ? "" : new String(password);
        boolean matches = PasswordEncoder.matches(rawPassword, credential.getPassword());
        if (!matches) {
            log.warn("设备密码错误: clientId={}, username={}", clientId, username);
            return Optional.of(false);
        }

        return Optional.of(true);
    }

    /**
     * 校验发布/订阅权限 (ACL)
     *
     * @param clientId 客户端ID
     * @param username 用户名
     * @param topic 目标主题
     * @param permission 权限类型 (PUB/SUB)
     * @return 是否允许
     */
    public boolean checkAcl(String clientId, String username, String topic, String permission) {
        String cacheKey = String.format("%s:%s:%s:%s", clientId, username, topic, permission);
        return aclCache.computeIfAbsent(cacheKey, key -> doCheckAcl(clientId, username, topic, permission));
    }

    private boolean doCheckAcl(String clientId, String username, String topic, String permission) {
        // 获取所有规则，按优先级从高到低排序
        List<MqttAclRuleEntity> rules = aclRuleRepository.findAllByOrderByPriorityDesc();

        for (MqttAclRuleEntity rule : rules) {
            // 1. 匹配客户端ID (支持 null/空/* 通配)
            if (rule.getClientId() != null && !rule.getClientId().isEmpty() && !rule.getClientId().equals("*")) {
                if (!rule.getClientId().equals(clientId)) {
                    continue;
                }
            }

            // 2. 匹配用户名 (支持 null/空/* 通配)
            if (rule.getUsername() != null && !rule.getUsername().isEmpty() && !rule.getUsername().equals("*")) {
                if (!rule.getUsername().equals(username)) {
                    continue;
                }
            }

            // 3. 匹配权限类型 (PUB_SUB 涵盖两者)
            String rulePerm = rule.getPermission();
            if (!"PUB_SUB".equalsIgnoreCase(rulePerm) && !permission.equalsIgnoreCase(rulePerm)) {
                continue;
            }

            // 4. 替换主题占位符
            String filter = rule.getTopic();
            filter = filter.replace("${clientId}", clientId);
            if (username != null) {
                filter = filter.replace("${username}", username);
            }

            // 5. 匹配主题通配符
            if (matchTopic(topic, filter)) {
                boolean allowed = "ALLOW".equalsIgnoreCase(rule.getAction());
                log.info("匹配到 ACL 规则: id={}, action={}, client={}, topic_filter={}",
                        rule.getId(), rule.getAction(), clientId, rule.getTopic());
                return allowed;
            }
        }

        // 如果没有匹配到任何规则，采用默认策略
        boolean defaultAllowed = "ALLOW".equalsIgnoreCase(defaultAclAction);
        if (log.isDebugEnabled()) {
            log.debug("未匹配到 ACL 规则，应用默认策略: default={}, client={}, topic={}, perm={}",
                    defaultAclAction, clientId, topic, permission);
        }
        return defaultAllowed;
    }

    /**
     * MQTT 主题匹配算法 (支持 + 和 #)
     */
    public static boolean matchTopic(String topic, String filter) {
        if (filter == null || filter.isEmpty()) {
            return false;
        }
        if (filter.equals("#")) {
            return true;
        }
        String[] topicParts = topic.split("/");
        String[] filterParts = filter.split("/");
        int ti = 0, fi = 0;
        while (ti < topicParts.length && fi < filterParts.length) {
            if (filterParts[fi].equals("#")) {
                return true;
            }
            if (!filterParts[fi].equals("+") && !filterParts[fi].equals(topicParts[ti])) {
                return false;
            }
            ti++;
            fi++;
        }
        return ti == topicParts.length && fi == filterParts.length;
    }

    /**
     * 清除 ACL 权限缓存 (每次添加/修改/删除 ACL 规则后调用)
     */
    public void invalidateCache() {
        log.info("清除 ACL 权限缓存");
        aclCache.clear();
    }

    // ================= CRUD API =================

    // 设备凭证管理
    public List<MqttClientCredentialEntity> findAllCredentials() {
        return credentialRepository.findAll();
    }

    public MqttClientCredentialEntity findCredentialById(Long id) {
        return credentialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("设备凭证不存在: " + id));
    }

    @Transactional
    public MqttClientCredentialEntity createCredential(MqttClientCredentialEntity entity) {
        // 密码加密
        if (entity.getPassword() != null && !entity.getPassword().isEmpty()) {
            entity.setPassword(PasswordEncoder.encode(entity.getPassword()));
        }
        return credentialRepository.save(entity);
    }

    @Transactional
    public MqttClientCredentialEntity updateCredential(Long id, MqttClientCredentialEntity updated) {
        MqttClientCredentialEntity existing = findCredentialById(id);
        existing.setClientId(updated.getClientId());
        existing.setUsername(updated.getUsername());
        if (updated.getPassword() != null && !updated.getPassword().isEmpty() && !updated.getPassword().equals(existing.getPassword())) {
            // 如果新密码不是加密后的密码，或者发生变化，就进行加密
            if (!PasswordEncoder.isBCryptHash(updated.getPassword())) {
                existing.setPassword(PasswordEncoder.encode(updated.getPassword()));
            } else {
                existing.setPassword(updated.getPassword());
            }
        }
        existing.setEnabled(updated.getEnabled());
        existing.setRemark(updated.getRemark());
        return credentialRepository.save(existing);
    }

    @Transactional
    public void deleteCredential(Long id) {
        credentialRepository.deleteById(id);
    }

    // ACL 规则管理
    public List<MqttAclRuleEntity> findAllAclRules() {
        return aclRuleRepository.findAllByOrderByPriorityDesc();
    }

    public MqttAclRuleEntity findAclRuleById(Long id) {
        return aclRuleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ACL 规则不存在: " + id));
    }

    @Transactional
    public MqttAclRuleEntity createAclRule(MqttAclRuleEntity entity) {
        MqttAclRuleEntity saved = aclRuleRepository.save(entity);
        invalidateCache();
        return saved;
    }

    @Transactional
    public MqttAclRuleEntity updateAclRule(Long id, MqttAclRuleEntity updated) {
        MqttAclRuleEntity existing = findAclRuleById(id);
        existing.setClientId(updated.getClientId());
        existing.setUsername(updated.getUsername());
        existing.setTopic(updated.getTopic());
        existing.setPermission(updated.getPermission());
        existing.setAction(updated.getAction());
        existing.setPriority(updated.getPriority());
        existing.setRemark(updated.getRemark());
        MqttAclRuleEntity saved = aclRuleRepository.save(existing);
        invalidateCache();
        return saved;
    }

    @Transactional
    public void deleteAclRule(Long id) {
        aclRuleRepository.deleteById(id);
        invalidateCache();
    }
}
