package com.jjc.mqtt.admin.persistence.controller;

import com.jjc.mqtt.admin.common.ResultPage;
import com.jjc.mqtt.admin.persistence.entity.MqttMessageRecordEntity;
import com.jjc.mqtt.admin.persistence.repository.MqttMessageRecordRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Tag(name = "消息记录", description = "MQTT消息存储与查询")
@RequestMapping("/v1/messages")
@RestController
public class MessageRecordController {

    private final MqttMessageRecordRepository messageRepository;

    public MessageRecordController(MqttMessageRecordRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Operation(summary = "查询消息列表")
    @GetMapping
    public ResponseEntity<ResultPage<MqttMessageRecordEntity>> queryMessages(
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String clientId,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        Specification<MqttMessageRecordEntity> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (topic != null && !topic.isEmpty()) {
                predicates.add(cb.like(root.get("topic"), "%" + topic + "%"));
            }
            if (clientId != null && !clientId.isEmpty()) {
                predicates.add(cb.like(root.get("clientId"), "%" + clientId + "%"));
            }
            if (startTime != null) {
                Instant startInstant = startTime.atZone(ZoneId.systemDefault()).toInstant();
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), startInstant));
            }
            if (endTime != null) {
                Instant endInstant = endTime.atZone(ZoneId.systemDefault()).toInstant();
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), endInstant));
            }
            if (keyword != null && !keyword.isEmpty()) {
                predicates.add(cb.like(root.get("payload"), "%" + keyword + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort = Sort.by(Sort.Direction.DESC, "timestamp");
        PageRequest pageRequest = PageRequest.of(Math.max(page - 1, 0), size, sort);

        Page<MqttMessageRecordEntity> pageResult = messageRepository.findAll(spec, pageRequest);
        return ResponseEntity.ok(ResultPage.of(pageResult));
    }

    @Operation(summary = "获取消息详情")
    @GetMapping("/{id}")
    public ResponseEntity<MqttMessageRecordEntity> getMessage(@PathVariable Long id) {
        return messageRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "删除指定消息")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        if (messageRepository.existsById(id)) {
            messageRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "清理过期消息")
    @DeleteMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupOldMessages(@RequestParam(defaultValue = "30") int days) {
        Instant cutoff = Instant.now().minusSeconds(days * 24L * 60L * 60);
        int deletedCount = messageRepository.deleteByTimestampBefore(cutoff);
        return ResponseEntity.ok(Map.of("deletedCount", deletedCount, "cutoffTime", cutoff));
    }

    @Operation(summary = "消息统计")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Instant now = Instant.now();
        Instant oneHourAgo = now.minus(1, ChronoUnit.HOURS);
        Instant thirtyMinutesAgo = now.minus(30, ChronoUnit.MINUTES);
        Instant fiveMinutesAgo = now.minus(5, ChronoUnit.MINUTES);

        long countLastHour = countByTimestampAfter(oneHourAgo);
        long countLast30Min = countByTimestampAfter(thirtyMinutesAgo);
        long countLast5Min = countByTimestampAfter(fiveMinutesAgo);

        return ResponseEntity.ok(Map.of(
            "last1Hour", countLastHour,
            "last30Min", countLast30Min,
            "last5Min", countLast5Min
        ));
    }

    @Operation(summary = "消息趋势")
    @GetMapping("/trend")
    public ResponseEntity<List<Map<String, Object>>> getTrend(@RequestParam(defaultValue = "60") int minutes) {
        Instant now = Instant.now();
        List<Map<String, Object>> trend = new ArrayList<>();

        for (int i = minutes - 1; i >= 0; i--) {
            Instant minuteStart = now.minus(i, ChronoUnit.MINUTES).truncatedTo(ChronoUnit.MINUTES);
            Instant minuteEnd = minuteStart.plus(1, ChronoUnit.MINUTES);
            long count = countByTimestampBetween(minuteStart, minuteEnd);
            trend.add(Map.of(
                "time", minuteStart.toString(),
                "count", count
            ));
        }

        return ResponseEntity.ok(trend);
    }

    private long countByTimestampAfter(Instant timestamp) {
        Specification<MqttMessageRecordEntity> spec = (root, cq, cb) ->
            cb.greaterThanOrEqualTo(root.get("timestamp"), timestamp);
        return messageRepository.count(spec);
    }

    private long countByTimestampBetween(Instant start, Instant end) {
        Specification<MqttMessageRecordEntity> spec = (root, cq, cb) ->
            cb.and(
                cb.greaterThanOrEqualTo(root.get("timestamp"), start),
                cb.lessThan(root.get("timestamp"), end)
            );
        return messageRepository.count(spec);
    }
}
