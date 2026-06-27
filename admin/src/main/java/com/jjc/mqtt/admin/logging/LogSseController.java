package com.jjc.mqtt.admin.logging;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Tag(name = "实时日志", description = "提供实时日志打印功能")
@Slf4j
@RestController
public class LogSseController {

    @Operation(summary = "获取实时日志（SSE）")
    @GetMapping(value = "/v1/logs/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamLogs() {
        SseEmitter emitter = new SseEmitter(0L);
        SseLogAppender.addEmitter(emitter);

        emitter.onCompletion(() -> SseLogAppender.removeEmitter(emitter));
        emitter.onTimeout(() -> SseLogAppender.removeEmitter(emitter));
        emitter.onError(error -> SseLogAppender.removeEmitter(emitter));

        try {
            emitter.send(SseEmitter.event().data("Connected to log stream"));
        } catch (IOException | IllegalStateException e) {
            SseLogAppender.removeEmitter(emitter);
            emitter.completeWithError(e);
        }

        log.info("SSE log stream connected, current client count: {}", SseLogAppender.getEmitterCount());
        return emitter;
    }
}
