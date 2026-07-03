package com.jjc.mqtt.admin.dashboard;

import com.jjc.mqtt.monitor.ClientInfo;
import com.jjc.mqtt.monitor.MonitorService;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.messages.InterceptConnectMessage;
import io.moquette.interception.messages.InterceptDisconnectMessage;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.netty.buffer.ByteBuf;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MetricsInterceptHandler extends AbstractInterceptHandler {

    private final MetricsCollector collector;
    private final MonitorService monitorService;
    private final AtomicInteger clientCount = new AtomicInteger(0);

    public MetricsInterceptHandler(MetricsCollector collector, MonitorService monitorService) {
        this.collector = collector;
        this.monitorService = monitorService;
    }

    @Override
    public String getID() {
        return "metrics_intercept_handler";
    }

    @Override
    public void onPublish(InterceptPublishMessage msg) {
        int qos = msg.getQos() != null ? msg.getQos().value() : 0;
        String topic = msg.getTopicName();

        ByteBuf buf = msg.getPayload();
        int payloadSize = buf != null ? buf.readableBytes() : 0;

        collector.incrementPublish(payloadSize);
        collector.incrementQos(qos);
        collector.incrementTopic(topic);

        int subscriberCount = 0;
        for (ClientInfo client : monitorService.getConnectedClients()) {
            for (String filter : client.getSubscriptions()) {
                if (TopicMatcher.matches(filter, topic)) {
                    subscriberCount++;
                    break;
                }
            }
        }
        if (subscriberCount > 0) {
            collector.incrementReceive(payloadSize * subscriberCount);
        }
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
    }
}
