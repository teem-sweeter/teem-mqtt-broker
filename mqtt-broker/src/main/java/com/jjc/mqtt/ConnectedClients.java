package com.jjc.mqtt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 已连接客户端 ID 跟踪器
 * <p>
 * 独立于 MonitorService，避免循环依赖。用于在认证阶段快速判断 Client ID 是否已连接。
 *
 * @author sweeter
 */
public class ConnectedClients {

    private static final Logger log = LoggerFactory.getLogger(ConnectedClients.class);

    private final Set<String> clientIds = ConcurrentHashMap.newKeySet();

    /**
     * 标记客户端已连接
     */
    public void add(String clientId) {
        clientIds.add(clientId);
        log.debug("ConnectedClients: 添加 {}", clientId);
    }

    /**
     * 标记客户端已断开
     */
    public void remove(String clientId) {
        clientIds.remove(clientId);
        log.debug("ConnectedClients: 移除 {}", clientId);
    }

    /**
     * 判断客户端是否已连接
     */
    public boolean contains(String clientId) {
        return clientIds.contains(clientId);
    }
}
