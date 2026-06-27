package com.jjc.mqtt.handler;

import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.messages.InterceptConnectMessage;
import io.moquette.interception.messages.InterceptConnectionLostMessage;
import io.moquette.interception.messages.InterceptDisconnectMessage;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultMoquetteHandler extends AbstractInterceptHandler {

    private static final Logger log = LoggerFactory.getLogger(DefaultMoquetteHandler.class);

    @Override
    public String getID() {
        return "default_moquette_handler";
    }

    @Override
    public void onConnect(InterceptConnectMessage msg) {
        log.info("设备已连接: ClientID={}, KeepAlive={}", msg.getClientID(), msg.getKeepAlive());
    }

    @Override
    public void onPublish(InterceptPublishMessage msg) {
        ByteBuf byteBuf = msg.getPayload();
        String payload = byteBuf.toString(CharsetUtil.UTF_8);
        log.info("收到消息: Topic={}, Payload={}", msg.getTopicName(), payload);
    }

    @Override
    public void onDisconnect(InterceptDisconnectMessage msg) {
        log.warn("设备断开连接: ClientID={}", msg.getClientID());
    }

    @Override
    public void onConnectionLost(InterceptConnectionLostMessage msg) {
        log.warn("设备连接丢失: ClientID={}", msg.getClientID());
    }

    @Override
    public void onSessionLoopError(Throwable throwable) {
        log.error("会话循环错误", throwable);
    }
}