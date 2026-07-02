package com.jjc.mqtt.admin.dashboard;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Tag(name = "Dashboard", description = "实时指标仪表盘")
@RestController
public class DashboardSseController {

    private static final Logger log = LoggerFactory.getLogger(DashboardSseController.class);

    private final MetricsCollector collector;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    public DashboardSseController(MetricsCollector collector) {
        this.collector = collector;
    }

    @Operation(summary = "实时指标数据流（SSE）")
    @GetMapping(value = "/api/dashboard/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMetrics(@RequestParam(defaultValue = "15m") String range) {
        SseEmitter emitter = new SseEmitter(0L);

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            try {
                List<MetricSnapshot> snapshots = collector.getSnapshots(range);
                log.debug("SSE pushing {} snapshots for range={}", snapshots.size(), range);
                emitter.send(SseEmitter.event()
                        .data(snapshots, MediaType.APPLICATION_JSON));
            } catch (IOException | IllegalStateException e) {
                log.error("SSE send error: {}", e.getMessage());
                emitter.complete();
            }
        }, 0, 1, TimeUnit.SECONDS);

        emitter.onCompletion(() -> future.cancel(false));
        emitter.onTimeout(() -> future.cancel(false));
        emitter.onError(e -> {
            log.error("SSE error: {}", e.getMessage());
            future.cancel(false);
        });

        log.info("Dashboard SSE connected, range={}", range);
        return emitter;
    }
}
