package com.jjc.mqtt.handler;

import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.messages.InterceptPublishMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端管控拦截处理器
 * <p>
 * 拦截禁止发送的客户端发布的消息，丢弃后不转发。
 *
 * @author sweeter
 */
public class ClientControlHandler extends AbstractInterceptHandler {

    private static final Logger log = LoggerFactory.getLogger(ClientControlHandler.class);

    /** 禁止发送的客户端ID集合 */
    private static final Set<String> sendDisabledClients = ConcurrentHashMap.newKeySet();

    @Override
    public String getID() {
        return "client_control_handler";
    }

    @Override
    public void onPublish(InterceptPublishMessage msg) {
        String clientId = msg.getClientID();
        if (sendDisabledClients.contains(clientId)) {
            // 丢弃该消息，不转发到任何订阅者
            log.debug("拦截禁止发送客户端的消息: clientId={}, topic={}", clientId, msg.getTopicName());
            // 注意：Moquette 的拦截器无法真正阻止消息路由，
            // 实际拦截需要通过 ClientControlService 在 publish 前检查
        }
    }

    @Override
    public void onSessionLoopError(Throwable throwable) {
        log.error("客户端管控拦截器会话循环错误", throwable);
    }

    public static void addSendDisabled(String clientId) {
        sendDisabledClients.add(clientId);
    }

    public static void removeSendDisabled(String clientId) {
        sendDisabledClients.remove(clientId);
    }

    public static boolean isSendDisabled(String clientId) {
        return sendDisabledClients.contains(clientId);
    }
}
