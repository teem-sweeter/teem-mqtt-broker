package com.jjc.mqtt.admin.dashboard;

import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.messages.InterceptConnectMessage;
import io.moquette.interception.messages.InterceptDisconnectMessage;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MetricsInterceptHandler extends AbstractInterceptHandler {

    private static final Logger log = LoggerFactory.getLogger(MetricsInterceptHandler.class);

    private final MetricsCollector collector;
    private final AtomicInteger clientCount = new AtomicInteger(0);

    public MetricsInterceptHandler(MetricsCollector collector) {
        this.collector = collector;
    }

    @Override
    public String getID() {
        return "metrics_intercept_handler";
    }

    @Override
    public void onPublish(InterceptPublishMessage msg) {
        ByteBuf buf = msg.getPayload();
        int payloadSize = buf != null ? buf.readableBytes() : 0;
        int qos = msg.getQos() != null ? msg.getQos().value() : 0;
        String topic = msg.getTopicName();

        collector.incrementPublish(payloadSize);
        collector.incrementQos(qos);
        collector.incrementTopic(topic);
        log.debug("Metrics onPublish: topic={}, qos={}, size={}", topic, qos, payloadSize);
    }

    @Override
    public void onConnect(InterceptConnectMessage msg) {
        int count = clientCount.incrementAndGet();
        collector.setActiveClients(count);
    }

    @Override
    public void onDisconnect(InterceptDisconnectMessage msg) {
        int count = clientCount.decrementAndGet();
        collector.setActiveClients(Math.max(0, count));
    }

    @Override
    public void onSessionLoopError(Throwable throwable) {
        collector.incrementErrors();
        log.error("Metrics intercept handler session error", throwable);
    }
}
