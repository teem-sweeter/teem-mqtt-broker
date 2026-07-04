package com.jjc.mqtt.handler;

import com.jjc.mqtt.bridge.BridgeEngine;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 桥接拦截处理器
 * <p>
 * 捕获本地 Broker 的发布事件，将匹配出向规则的消息投递到桥接引擎。
 * 注册为 Spring Bean 后会被 Moquette 自动收集。
 *
 * @author sweeter
 */
public class BridgeInterceptHandler extends AbstractInterceptHandler {

    private static final Logger log = LoggerFactory.getLogger(BridgeInterceptHandler.class);

    /** bridgeId -> BridgeEngine 映射 */
    private static final Map<String, BridgeEngine> engines = new ConcurrentHashMap<>();

    @Override
    public String getID() {
        return "bridge_intercept_handler";
    }

    @Override
    public void onPublish(InterceptPublishMessage msg) {
        try {
            String clientId = msg.getClientID();
            String topic = msg.getTopicName();
            int qos = msg.getQos() != null ? msg.getQos().value() : 0;
            boolean retained = msg.isRetainFlag();

            // 防环：如果是桥接入向注入的消息，直接跳过
            if (BridgeEngine.BRIDGE_INBOUND_AGENT.equals(clientId)) {
                return;
            }

            // 将 payload 转为字节数组
            io.netty.buffer.ByteBuf buf = msg.getPayload();
            byte[] payload = new byte[buf.readableBytes()];
            buf.getBytes(buf.readerIndex(), payload);

            // 通知所有活跃的桥接引擎
            for (BridgeEngine engine : engines.values()) {
                if (engine.isRunning()) {
                    engine.onLocalPublish(topic, payload, qos, retained, clientId);
                }
            }
        } finally {
            io.netty.util.ReferenceCountUtil.safeRelease(msg.getPayload());
        }
    }

    @Override
    public void onSessionLoopError(Throwable throwable) {
        log.error("桥接拦截器会话循环错误", throwable);
    }

    /**
     * 注册桥接引擎
     */
    public static void registerEngine(String bridgeId, BridgeEngine engine) {
        engines.put(bridgeId, engine);
        log.info("桥接引擎已注册到拦截器: bridgeId={}", bridgeId);
    }

    /**
     * 注销桥接引擎
     */
    public static void unregisterEngine(String bridgeId) {
        engines.remove(bridgeId);
        log.info("桥接引擎已从拦截器注销: bridgeId={}", bridgeId);
    }
}
