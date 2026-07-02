package com.jjc.mqtt.admin.dashboard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

@Service
@EnableScheduling
public class MetricsCollector {

    private static final Logger log = LoggerFactory.getLogger(MetricsCollector.class);

    private final AtomicLong publishCount = new AtomicLong(0);
    private final AtomicLong receiveCount = new AtomicLong(0);
    private final AtomicLong bytesIn = new AtomicLong(0);
    private final AtomicLong bytesOut = new AtomicLong(0);
    private final AtomicLong qos0Count = new AtomicLong(0);
    private final AtomicLong qos1Count = new AtomicLong(0);
    private final AtomicLong qos2Count = new AtomicLong(0);
    private final AtomicLong errorsCount = new AtomicLong(0);
    private final ConcurrentHashMap<String, AtomicLong> topicCounts = new ConcurrentHashMap<>();
    private volatile int activeClients = 0;

    private long lastPublishCount = 0;
    private long lastReceiveCount = 0;
    private long lastBytesIn = 0;
    private long lastBytesOut = 0;
    private long lastQos0 = 0;
    private long lastQos1 = 0;
    private long lastQos2 = 0;
    private long lastErrors = 0;

    private final ConcurrentLinkedDeque<MetricSnapshot> buffer = new ConcurrentLinkedDeque<>();
    private static final int MAX_BUFFER_SIZE = 3600;

    public void incrementPublish(int payloadSize) {
        publishCount.incrementAndGet();
        bytesIn.addAndGet(payloadSize);
        log.debug("incrementPublish: payloadSize={}, publishCount={}", payloadSize, publishCount.get());
    }

    public void incrementReceive(int payloadSize) {
        receiveCount.incrementAndGet();
        bytesOut.addAndGet(payloadSize);
    }

    public void incrementQos(int qos) {
        switch (qos) {
            case 0 -> qos0Count.incrementAndGet();
            case 1 -> qos1Count.incrementAndGet();
            case 2 -> qos2Count.incrementAndGet();
        }
    }

    public void incrementTopic(String topic) {
        topicCounts.computeIfAbsent(topic, k -> new AtomicLong(0)).incrementAndGet();
    }

    public void incrementErrors() {
        errorsCount.incrementAndGet();
    }

    public void setActiveClients(int count) {
        this.activeClients = count;
    }

    @Scheduled(fixedRate = 1000)
    public void snapshot() {
        long now = System.currentTimeMillis();
        long currentPublish = publishCount.get();
        long currentReceive = receiveCount.get();
        long currentBytesIn = bytesIn.get();
        long currentBytesOut = bytesOut.get();
        long currentQos0 = qos0Count.get();
        long currentQos1 = qos1Count.get();
        long currentQos2 = qos2Count.get();
        long currentErrors = errorsCount.get();

        long publishDelta = currentPublish - lastPublishCount;
        long receiveDelta = currentReceive - lastReceiveCount;

        MetricSnapshot snap = new MetricSnapshot(
                now,
                publishDelta,
                receiveDelta,
                currentBytesIn - lastBytesIn,
                currentBytesOut - lastBytesOut,
                activeClients,
                currentQos0 - lastQos0,
                currentQos1 - lastQos1,
                currentQos2 - lastQos2,
                currentErrors - lastErrors,
                getTopTopics(10)
        );

        lastPublishCount = currentPublish;
        lastReceiveCount = currentReceive;
        lastBytesIn = currentBytesIn;
        lastBytesOut = currentBytesOut;
        lastQos0 = currentQos0;
        lastQos1 = currentQos1;
        lastQos2 = currentQos2;
        lastErrors = currentErrors;

        buffer.addLast(snap);
        while (buffer.size() > MAX_BUFFER_SIZE) {
            buffer.pollFirst();
        }

        if (publishDelta > 0 || receiveDelta > 0) {
            log.info("Metrics snapshot: publish={}, receive={}, clients={}, bufferSize={}", publishDelta, receiveDelta, activeClients, buffer.size());
        }
    }

    private List<MetricSnapshot.TopicCount> getTopTopics(int limit) {
        return topicCounts.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue().get(), a.getValue().get()))
                .limit(limit)
                .map(e -> new MetricSnapshot.TopicCount(e.getKey(), e.getValue().get()))
                .toList();
    }

    public List<MetricSnapshot> getSnapshots(String range) {
        long now = System.currentTimeMillis();
        long durationMs = switch (range) {
            case "5m" -> 5 * 60 * 1000L;
            case "60m" -> 60 * 60 * 1000L;
            default -> 15 * 60 * 1000L;
        };
        long cutoff = now - durationMs;

        return buffer.stream()
                .filter(s -> s.getTimestamp() >= cutoff)
                .toList();
    }
}
