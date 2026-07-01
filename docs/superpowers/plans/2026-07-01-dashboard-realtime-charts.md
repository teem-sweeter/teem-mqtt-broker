# Dashboard 实时图表 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将现有静态 Dashboard 改造为实时图表仪表盘，展示 6 个 ECharts 图表，数据通过 SSE 每秒推送。

**Architecture:** 自定义 `MetricsCollector` 服务 + Moquette 拦截器 + 内存环形缓冲 + SSE 端点 + ECharts 前端。

**Tech Stack:** Spring Boot 4.0.6, Moquette 0.18.0, ECharts + vue-echarts, Vue 3, Element Plus

## Global Constraints

- 项目使用 Java 21 + Spring Boot 4.0.6
- 前端使用 Vue 3 + Element Plus + vue-i18n
- 所有新 API 需添加 Swagger 注解（`@Tag`, `@Operation`）
- 前端需要 i18n（zh-CN + en-US）
- 遵循现有代码风格（Lombok, 构造器注入）

---

## 文件结构

### 后端（新建）

| 文件 | 职责 |
|------|------|
| `admin/.../dashboard/MetricsCollector.java` | 指标采集服务：原子计数器、快照生成、环形缓冲 |
| `admin/.../dashboard/MetricsInterceptHandler.java` | Moquette 拦截器：递增计数器 |
| `admin/.../dashboard/DashboardSseController.java` | SSE 端点：推送指标数据 |
| `admin/.../dashboard/MetricSnapshot.java` | 数据模型：快照结构 |

### 后端（修改）

| 文件 | 修改内容 |
|------|----------|
| `ui-webjar/package.json` | 添加 echarts + vue-echarts 依赖 |

### 前端（新建）

| 文件 | 职责 |
|------|------|
| `ui-webjar/src/views/Dashboard/components/MessageChart.vue` | 消息吞吐量折线图 |
| `ui-webjar/src/views/Dashboard/components/ClientChart.vue` | 客户端连接数折线图 |
| `ui-webjar/src/views/Dashboard/components/BandwidthChart.vue` | 流量带宽面积图 |
| `ui-webjar/src/views/Dashboard/components/TopicChart.vue` | Topic 热力柱状图 |
| `ui-webjar/src/views/Dashboard/components/QosChart.vue` | QoS 分布饼图 |
| `ui-webjar/src/views/Dashboard/components/ErrorChart.vue` | 错误率折线图 |

### 前端（修改）

| 文件 | 修改内容 |
|------|----------|
| `ui-webjar/src/views/Dashboard/index.vue` | 重构布局：概览卡片 + 6 图表 + SSE 数据源 |
| `ui-webjar/src/i18n/locales/zh-CN.json` | 添加图表相关翻译 |
| `ui-webjar/src/i18n/locales/en-US.json` | 添加图表相关翻译 |

---

## Task 1: MetricSnapshot 数据模型

**Files:**
- Create: `admin/src/main/java/com/jjc/mqtt/admin/dashboard/MetricSnapshot.java`

- [ ] **Step 1: 创建 MetricSnapshot 类**

```java
package com.jjc.mqtt.admin.dashboard;

import java.util.List;

public class MetricSnapshot {
    private long timestamp;
    private long publishDelta;
    private long receiveDelta;
    private long bytesInDelta;
    private long bytesOutDelta;
    private int activeClients;
    private long qos0Delta;
    private long qos1Delta;
    private long qos2Delta;
    private long errorsDelta;
    private List<TopicCount> topTopics;

    public MetricSnapshot() {}

    public MetricSnapshot(long timestamp, long publishDelta, long receiveDelta,
                          long bytesInDelta, long bytesOutDelta, int activeClients,
                          long qos0Delta, long qos1Delta, long qos2Delta,
                          long errorsDelta, List<TopicCount> topTopics) {
        this.timestamp = timestamp;
        this.publishDelta = publishDelta;
        this.receiveDelta = receiveDelta;
        this.bytesInDelta = bytesInDelta;
        this.bytesOutDelta = bytesOutDelta;
        this.activeClients = activeClients;
        this.qos0Delta = qos0Delta;
        this.qos1Delta = qos1Delta;
        this.qos2Delta = qos2Delta;
        this.errorsDelta = errorsDelta;
        this.topTopics = topTopics;
    }

    // Getters and Setters
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public long getPublishDelta() { return publishDelta; }
    public void setPublishDelta(long publishDelta) { this.publishDelta = publishDelta; }
    public long getReceiveDelta() { return receiveDelta; }
    public void setReceiveDelta(long receiveDelta) { this.receiveDelta = receiveDelta; }
    public long getBytesInDelta() { return bytesInDelta; }
    public void setBytesInDelta(long bytesInDelta) { this.bytesInDelta = bytesInDelta; }
    public long getBytesOutDelta() { return bytesOutDelta; }
    public void setBytesOutDelta(long bytesOutDelta) { this.bytesOutDelta = bytesOutDelta; }
    public int getActiveClients() { return activeClients; }
    public void setActiveClients(int activeClients) { this.activeClients = activeClients; }
    public long getQos0Delta() { return qos0Delta; }
    public void setQos0Delta(long qos0Delta) { this.qos0Delta = qos0Delta; }
    public long getQos1Delta() { return qos1Delta; }
    public void setQos1Delta(long qos1Delta) { this.qos1Delta = qos1Delta; }
    public long getQos2Delta() { return qos2Delta; }
    public void setQos2Delta(long qos2Delta) { this.qos2Delta = qos2Delta; }
    public long getErrorsDelta() { return errorsDelta; }
    public void setErrorsDelta(long errorsDelta) { this.errorsDelta = errorsDelta; }
    public List<TopicCount> getTopTopics() { return topTopics; }
    public void setTopTopics(List<TopicCount> topTopics) { this.topTopics = topTopics; }

    public static class TopicCount {
        private String topic;
        private long count;

        public TopicCount() {}

        public TopicCount(String topic, long count) {
            this.topic = topic;
            this.count = count;
        }

        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add admin/src/main/java/com/jjc/mqtt/admin/dashboard/MetricSnapshot.java
git commit -m "feat(dashboard): add MetricSnapshot data model"
```

---

## Task 2: MetricsCollector 指标采集服务

**Files:**
- Create: `admin/src/main/java/com/jjc/mqtt/admin/dashboard/MetricsCollector.java`

**Interfaces:**
- Consumes: `MetricSnapshot` (Task 1)
- Produces: `MetricsCollector.snapshot()`, `MetricsCollector.incrementPublish()`, etc.

- [ ] **Step 1: 创建 MetricsCollector 类**

```java
package com.jjc.mqtt.admin.dashboard;

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

    // 上一次快照的累计值（用于计算增量）
    private long lastPublishCount = 0;
    private long lastReceiveCount = 0;
    private long lastBytesIn = 0;
    private long lastBytesOut = 0;
    private long lastQos0 = 0;
    private long lastQos1 = 0;
    private long lastQos2 = 0;
    private long lastErrors = 0;

    // 环形缓冲：最大 3600 条（1 小时 @ 1 条/秒）
    private final ConcurrentLinkedDeque<MetricSnapshot> buffer = new ConcurrentLinkedDeque<>();
    private static final int MAX_BUFFER_SIZE = 3600;

    public void incrementPublish(int payloadSize) {
        publishCount.incrementAndGet();
        bytesIn.addAndGet(payloadSize);
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

    public void clientConnected() {
        // 由 MetricsInterceptHandler 维护
    }

    public void clientDisconnected() {
        // 由 MetricsInterceptHandler 维护
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

        MetricSnapshot snap = new MetricSnapshot(
                now,
                currentPublish - lastPublishCount,
                currentReceive - lastReceiveCount,
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
```

- [ ] **Step 2: Commit**

```bash
git add admin/src/main/java/com/jjc/mqtt/admin/dashboard/MetricsCollector.java
git commit -m "feat(dashboard): add MetricsCollector service"
```

---

## Task 3: MetricsInterceptHandler 拦截器

**Files:**
- Create: `admin/src/main/java/com/jjc/mqtt/admin/dashboard/MetricsInterceptHandler.java`

**Interfaces:**
- Consumes: `MetricsCollector` (Task 2)
- Produces: Moquette `InterceptHandler` bean，自动注册

- [ ] **Step 1: 创建 MetricsInterceptHandler 类**

```java
package com.jjc.mqtt.admin.dashboard;

import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.messages.InterceptConnectMessage;
import io.moquette.interception.messages.InterceptDisconnectMessage;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.netty.buffer.ByteBuf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MetricsInterceptHandler extends AbstractInterceptHandler {

    private static final Logger log = LoggerFactory.getLogger(MetricsInterceptHandler.class);

    private final MetricsCollector collector;
    private final java.util.concurrent.atomic.AtomicInteger clientCount = new java.util.concurrent.atomic.AtomicInteger(0);

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
```

- [ ] **Step 2: Commit**

```bash
git add admin/src/main/java/com/jjc/mqtt/admin/dashboard/MetricsInterceptHandler.java
git commit -m "feat(dashboard): add MetricsInterceptHandler"
```

---

## Task 4: DashboardSseController SSE 端点

**Files:**
- Create: `admin/src/main/java/com/jjc/mqtt/admin/dashboard/DashboardSseController.java`

**Interfaces:**
- Consumes: `MetricsCollector.getSnapshots()` (Task 2)
- Produces: `GET /api/dashboard/stream?range=15m`

- [ ] **Step 1: 创建 DashboardSseController 类**

```java
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
                emitter.send(SseEmitter.event()
                        .data(snapshots, MediaType.APPLICATION_JSON));
            } catch (IOException | IllegalStateException e) {
                emitter.complete();
            }
        }, 0, 1, TimeUnit.SECONDS);

        emitter.onCompletion(() -> future.cancel(false));
        emitter.onTimeout(() -> future.cancel(false));
        emitter.onError(e -> future.cancel(false));

        log.info("Dashboard SSE connected, range={}", range);
        return emitter;
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add admin/src/main/java/com/jjc/mqtt/admin/dashboard/DashboardSseController.java
git commit -m "feat(dashboard): add DashboardSseController SSE endpoint"
```

---

## Task 5: 前端安装 ECharts 依赖

**Files:**
- Modify: `ui-webjar/package.json`

- [ ] **Step 1: 安装 echarts 和 vue-echarts**

```bash
cd ui-webjar && npm install echarts vue-echarts
```

- [ ] **Step 2: Commit**

```bash
git add ui-webjar/package.json ui-webjar/package-lock.json
git commit -m "feat(dashboard): add echarts and vue-echarts dependencies"
```

---

## Task 6: 前端 Dashboard 重构

**Files:**
- Modify: `ui-webjar/src/views/Dashboard/index.vue`
- Create: `ui-webjar/src/views/Dashboard/components/MessageChart.vue`
- Create: `ui-webjar/src/views/Dashboard/components/ClientChart.vue`
- Create: `ui-webjar/src/views/Dashboard/components/BandwidthChart.vue`
- Create: `ui-webjar/src/views/Dashboard/components/TopicChart.vue`
- Create: `ui-webjar/src/views/Dashboard/components/QosChart.vue`
- Create: `ui-webjar/src/views/Dashboard/components/ErrorChart.vue`
- Modify: `ui-webjar/src/i18n/locales/zh-CN.json`
- Modify: `ui-webjar/src/i18n/locales/en-US.json`

- [ ] **Step 1: 创建 MessageChart.vue**

```vue
<template>
  <div class="chart-card">
    <div class="chart-title">{{ t('dashboard.messageChart') }}</div>
    <v-chart :option="chartOption" autoresize style="height: 280px" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import VChart from 'vue-echarts'

const { t } = useI18n()

const props = defineProps({
  data: { type: Array, default: () => [] }
})

const chartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: [t('dashboard.publishRate'), t('dashboard.receiveRate')], bottom: 0 },
  xAxis: { type: 'category', data: props.data.map(d => new Date(d.timestamp).toLocaleTimeString()) },
  yAxis: { type: 'value', name: t('dashboard.msgPerSec') },
  series: [
    { name: t('dashboard.publishRate'), type: 'line', smooth: true, data: props.data.map(d => d.publishDelta) },
    { name: t('dashboard.receiveRate'), type: 'line', smooth: true, data: props.data.map(d => d.receiveDelta) }
  ]
}))
</script>
```

- [ ] **Step 2: 创建 ClientChart.vue**

```vue
<template>
  <div class="chart-card">
    <div class="chart-title">{{ t('dashboard.clientChart') }}</div>
    <v-chart :option="chartOption" autoresize style="height: 280px" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import VChart from 'vue-echarts'

const { t } = useI18n()

const props = defineProps({
  data: { type: Array, default: () => [] }
})

const chartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: props.data.map(d => new Date(d.timestamp).toLocaleTimeString()) },
  yAxis: { type: 'value', name: t('dashboard.clients') },
  series: [
    { name: t('dashboard.activeClients'), type: 'line', smooth: true, areaStyle: { opacity: 0.3 }, data: props.data.map(d => d.activeClients) }
  ]
}))
</script>
```

- [ ] **Step 3: 创建 BandwidthChart.vue**

```vue
<template>
  <div class="chart-card">
    <div class="chart-title">{{ t('dashboard.bandwidthChart') }}</div>
    <v-chart :option="chartOption" autoresize style="height: 280px" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import VChart from 'vue-echarts'

const { t } = useI18n()

const props = defineProps({
  data: { type: Array, default: () => [] }
})

const chartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: [t('dashboard.bytesIn'), t('dashboard.bytesOut')], bottom: 0 },
  xAxis: { type: 'category', data: props.data.map(d => new Date(d.timestamp).toLocaleTimeString()) },
  yAxis: { type: 'value', name: t('dashboard.bytesPerSec') },
  series: [
    { name: t('dashboard.bytesIn'), type: 'line', smooth: true, areaStyle: { opacity: 0.3 }, data: props.data.map(d => d.bytesInDelta) },
    { name: t('dashboard.bytesOut'), type: 'line', smooth: true, areaStyle: { opacity: 0.3 }, data: props.data.map(d => d.bytesOutDelta) }
  ]
}))
</script>
```

- [ ] **Step 4: 创建 TopicChart.vue**

```vue
<template>
  <div class="chart-card">
    <div class="chart-title">{{ t('dashboard.topicChart') }}</div>
    <v-chart :option="chartOption" autoresize style="height: 280px" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import VChart from 'vue-echarts'

const { t } = useI18n()

const props = defineProps({
  data: { type: Array, default: () => [] }
})

const chartOption = computed(() => {
  const latest = props.data.length > 0 ? props.data[props.data.length - 1] : null
  const topics = latest?.topTopics || []
  return {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: topics.map(t => t.topic), axisLabel: { rotate: 30 } },
    yAxis: { type: 'value', name: t('dashboard.messageCount') },
    series: [
      { name: t('dashboard.messageCount'), type: 'bar', data: topics.map(t => t.count) }
    ]
  }
})
</script>
```

- [ ] **Step 5: 创建 QosChart.vue**

```vue
<template>
  <div class="chart-card">
    <div class="chart-title">{{ t('dashboard.qosChart') }}</div>
    <v-chart :option="chartOption" autoresize style="height: 280px" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import VChart from 'vue-echarts'

const { t } = useI18n()

const props = defineProps({
  data: { type: Array, default: () => [] }
})

const chartOption = computed(() => {
  const totals = { qos0: 0, qos1: 0, qos2: 0 }
  props.data.forEach(d => {
    totals.qos0 += d.qos0Delta || 0
    totals.qos1 += d.qos1Delta || 0
    totals.qos2 += d.qos2Delta || 0
  })
  return {
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: [
        { name: 'QoS 0', value: totals.qos0 },
        { name: 'QoS 1', value: totals.qos1 },
        { name: 'QoS 2', value: totals.qos2 }
      ]
    }]
  }
})
</script>
```

- [ ] **Step 6: 创建 ErrorChart.vue**

```vue
<template>
  <div class="chart-card">
    <div class="chart-title">{{ t('dashboard.errorChart') }}</div>
    <v-chart :option="chartOption" autoresize style="height: 280px" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import VChart from 'vue-echarts'

const { t } = useI18n()

const props = defineProps({
  data: { type: Array, default: () => [] }
})

const chartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: props.data.map(d => new Date(d.timestamp).toLocaleTimeString()) },
  yAxis: { type: 'value', name: t('dashboard.errorsPerSec') },
  series: [
    { name: t('dashboard.errorRate'), type: 'line', smooth: true, data: props.data.map(d => d.errorsDelta) }
  ]
}))
</script>
```

- [ ] **Step 7: 重构 Dashboard/index.vue**

```vue
<template>
  <div class="dashboard-page">
    <div class="container">
      <div class="dashboard-header">
        <div class="range-switch">
          <el-radio-group v-model="timeRange" @change="reconnect">
            <el-radio-button value="5m">5 {{ t('dashboard.minutes') }}</el-radio-button>
            <el-radio-button value="15m">15 {{ t('dashboard.minutes') }}</el-radio-button>
            <el-radio-button value="60m">60 {{ t('dashboard.minutes') }}</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <div class="overview-cards">
        <MetricCards :mqtt-info="mqttInfo" :has-mqtt="hasMqtt" />
      </div>

      <div class="charts-grid">
        <MessageChart :data="chartData" />
        <ClientChart :data="chartData" />
        <BandwidthChart :data="chartData" />
        <TopicChart :data="chartData" />
        <QosChart :data="chartData" />
        <ErrorChart :data="chartData" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { getHealth } from '@/api/mqtt'
import MetricCards from './components/MetricCards.vue'
import MessageChart from './components/MessageChart.vue'
import ClientChart from './components/ClientChart.vue'
import BandwidthChart from './components/BandwidthChart.vue'
import TopicChart from './components/TopicChart.vue'
import QosChart from './components/QosChart.vue'
import ErrorChart from './components/ErrorChart.vue'

const { t } = useI18n()

const timeRange = ref('15m')
const chartData = ref([])
const mqttInfo = ref({})
const hasMqtt = ref(false)
let eventSource = null

function connectSse() {
  if (eventSource) {
    eventSource.close()
  }
  eventSource = new EventSource(`/api/dashboard/stream?range=${timeRange.value}`)
  eventSource.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      chartData.value = data
    } catch (e) {
      console.error('SSE parse error:', e)
    }
  }
  eventSource.onerror = () => {
    console.error('SSE connection error')
  }
}

function reconnect() {
  connectSse()
}

async function loadHealth() {
  try {
    mqttInfo.value = await getHealth()
    hasMqtt.value = true
  } catch (e) {
    console.error('Failed to load health:', e)
  }
}

onMounted(() => {
  connectSse()
  loadHealth()
})

onUnmounted(() => {
  if (eventSource) {
    eventSource.close()
  }
})
</script>

<style scoped>
.dashboard-page {
  padding: 16px;
}
.container {
  max-width: 1400px;
  margin: 0 auto;
}
.dashboard-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}
.overview-cards {
  margin-bottom: 16px;
}
.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}
@media (max-width: 768px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
}
.chart-card {
  background: var(--el-bg-color);
  border-radius: 8px;
  padding: 16px;
  box-shadow: var(--el-box-shadow-light);
}
.chart-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
  color: var(--el-text-color-primary);
}
</style>
```

- [ ] **Step 8: 添加 i18n 翻译**

zh-CN.json 添加：
```json
"dashboard": {
  "messageChart": "消息吞吐量",
  "clientChart": "客户端连接数",
  "bandwidthChart": "流量带宽",
  "topicChart": "Topic 热力图",
  "qosChart": "QoS 分布",
  "errorChart": "错误率",
  "publishRate": "发布速率",
  "receiveRate": "接收速率",
  "msgPerSec": "消息/秒",
  "clients": "客户端数",
  "activeClients": "活跃客户端",
  "bytesIn": "入站流量",
  "bytesOut": "出站流量",
  "bytesPerSec": "字节/秒",
  "messageCount": "消息数",
  "errorRate": "错误率",
  "errorsPerSec": "错误/秒",
  "minutes": "分钟"
}
```

en-US.json 添加：
```json
"dashboard": {
  "messageChart": "Message Throughput",
  "clientChart": "Client Connections",
  "bandwidthChart": "Traffic Bandwidth",
  "topicChart": "Topic Heatmap",
  "qosChart": "QoS Distribution",
  "errorChart": "Error Rate",
  "publishRate": "Publish Rate",
  "receiveRate": "Receive Rate",
  "msgPerSec": "msg/s",
  "clients": "Clients",
  "activeClients": "Active Clients",
  "bytesIn": "Bytes In",
  "bytesOut": "Bytes Out",
  "bytesPerSec": "bytes/s",
  "messageCount": "Messages",
  "errorRate": "Error Rate",
  "errorsPerSec": "errors/s",
  "minutes": "min"
}
```

- [ ] **Step 9: Commit**

```bash
git add ui-webjar/src/views/Dashboard/ ui-webjar/src/i18n/
git commit -m "feat(dashboard): implement real-time charts with ECharts and SSE"
```

---

## Task 7: 构建验证

- [ ] **Step 1: 安装前端依赖并构建**

```bash
cd ui-webjar && npm install && npm run build
```

- [ ] **Step 2: Maven 构建**

```bash
cd .. && mvn clean package -DskipTests
```

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "chore: verify build passes"
```
