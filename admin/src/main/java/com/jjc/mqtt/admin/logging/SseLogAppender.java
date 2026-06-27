package com.jjc.mqtt.admin.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SseLogAppender extends AppenderBase<ILoggingEvent> {

    private static final Set<SseEmitter> emitters = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private Layout<ILoggingEvent> layout;

    public void setLayout(Layout<ILoggingEvent> layout) {
        this.layout = layout;
    }

    public static void addEmitter(SseEmitter emitter) {
        emitters.add(emitter);
    }

    public static void removeEmitter(SseEmitter emitter) {
        emitters.remove(emitter);
    }

    public static int getEmitterCount() {
        return emitters.size();
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (layout == null || emitters.isEmpty()) {
            return;
        }

        String logMessage = layout.doLayout(event);
        sendToAllEmitters(logMessage);
    }

    private void sendToAllEmitters(String message) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .data(message, MediaType.TEXT_PLAIN));
            } catch (IOException | IllegalStateException e) {
                emitters.remove(emitter);
                emitter.complete();
            }
        }
    }
}
