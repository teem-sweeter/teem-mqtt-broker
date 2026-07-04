package com.jjc.mqtt;

import com.jjc.mqtt.monitor.ClientControlProvider;
import com.jjc.mqtt.monitor.MonitorService;
import io.moquette.broker.security.IAuthenticator;
import io.moquette.broker.security.IAuthorizatorPolicy;
import io.moquette.broker.subscriptions.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;

/**
 * @author sweeter
 * @date 2026/2/4 14:36
 * @description
 */
public class DefaultMoquetteAuthenticator implements IAuthenticator, IAuthorizatorPolicy {

    private static final Logger log = LoggerFactory.getLogger(DefaultMoquetteAuthenticator.class);

    private final String expectedUser;
    private final String expectedPass;
    private final boolean allowAnonymous;
    private final DuplicateClientIdStrategy duplicateClientIdStrategy;
    private final ObjectProvider<ConnectedClients> connectedClientsProvider;
    private final ObjectProvider<MonitorService> monitorServiceProvider;
    private final ObjectProvider<ClientControlProvider> clientControlProvider;

    public DefaultMoquetteAuthenticator(String user, String pass, boolean allowAnonymous,
                                        DuplicateClientIdStrategy duplicateClientIdStrategy,
                                        ObjectProvider<ConnectedClients> connectedClientsProvider,
                                        ObjectProvider<MonitorService> monitorServiceProvider,
                                        ObjectProvider<ClientControlProvider> clientControlProvider) {
        this.expectedUser = user;
        this.expectedPass = pass;
        this.allowAnonymous = allowAnonymous;
        this.duplicateClientIdStrategy = duplicateClientIdStrategy;
        this.connectedClientsProvider = connectedClientsProvider;
        this.monitorServiceProvider = monitorServiceProvider;
        this.clientControlProvider = clientControlProvider;
    }

    @Override
    public boolean checkValid(String clientId, String username, byte[] password) {
        // 匿名连接：用户名为空且允许匿名
        if (allowAnonymous && (username == null || username.isEmpty() ||password==null ||new String(password).isEmpty())) {
            log.debug("匿名连接: ClientID={}", clientId);
            return true;
        }

        // 非匿名连接：校验用户名密码
        if (!expectedUser.equals(username) || !PasswordEncoder.matches(new String(password), expectedPass)) {
            return false;
        }

        ConnectedClients connectedClients = connectedClientsProvider.getIfAvailable();
        if (connectedClients != null && connectedClients.contains(clientId)) {
            switch (duplicateClientIdStrategy) {
                case REJECT_NEW -> {
                    log.warn("拒绝新连接: ClientID={} 已存在活跃连接", clientId);
                    return false;
                }
                case DISCONNECT_OLD -> {
                    log.info("断开旧连接: ClientID={}, 接受新连接", clientId);
                    connectedClients.remove(clientId);
                    MonitorService monitorService = monitorServiceProvider.getIfAvailable();
                    if (monitorService != null) {
                        monitorService.disconnectClient(clientId);
                    }
                }
            }
        }

        return true;
    }

    @Override
    public boolean canWrite(Topic topic, String username, String clientId) {
        ClientControlProvider ccp = clientControlProvider.getIfAvailable();
        if (ccp != null && ccp.isSendDisabled(clientId)) {
            log.warn("拒绝发布: clientId={}, topic={} (禁止发送)", clientId, topic);
            return false;
        }
        return true;
    }

    @Override
    public boolean canRead(Topic topic, String username, String clientId) {
        // 允许订阅，但实际接收消息会通过动态代理过滤
        return true;
    }

    public boolean isReceiveDisabled(String clientId) {
        ClientControlProvider ccp = clientControlProvider.getIfAvailable();
        return ccp != null && ccp.isReceiveDisabled(clientId);
    }
}
