package com.jjc.mqtt.admin.webhook.dispatch;

import com.jjc.mqtt.admin.webhook.entity.WebHookEntity;
import com.jjc.mqtt.admin.webhook.service.WebHookService;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class WebHookDispatchHandler extends AbstractInterceptHandler {

    private static final Logger log = LoggerFactory.getLogger(WebHookDispatchHandler.class);

    private final WebHookService webHookService;
    private final WebHookDispatcher dispatcher;

    public WebHookDispatchHandler(WebHookService webHookService, WebHookDispatcher dispatcher) {
        this.webHookService = webHookService;
        this.dispatcher = dispatcher;
    }

    @Override
    public String getID() {
        return "webhook_dispatch_handler";
    }

    @Override
    public void onPublish(InterceptPublishMessage msg) {
        String topic = msg.getTopicName();
        int qos = msg.getQos() != null ? msg.getQos().value() : 0;
        String clientId = msg.getClientID();

        ByteBuf buf = msg.getPayload();
        byte[] payload = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), payload);

        List<WebHookEntity> webhooks = webHookService.getActiveWebhooks();
        for (WebHookEntity wh : webhooks) {
            if (topicMatches(topic, wh.getTopicFilter()) && qos >= wh.getQos()) {
                dispatcher.enqueue(wh, topic, payload, clientId, qos, msg.isRetainFlag());
            }
        }
    }

    private boolean topicMatches(String topic, String filter) {
        if (filter == null || filter.isEmpty() || filter.equals("#")) {
            return true;
        }
        return matchTopics(topic, filter);
    }

    private boolean matchTopics(String topic, String filter) {
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

    @Override
    public void onSessionLoopError(Throwable throwable) {
        log.error("WebHook dispatch handler session error", throwable);
    }
}
