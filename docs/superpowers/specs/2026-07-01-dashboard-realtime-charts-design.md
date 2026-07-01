# Dashboard 实时图表设计

## 概述

将现有静态 Dashboard 页面改造为实时图表仪表盘，展示 MQTT Broker 运行时关键指标。

**目标**：
- 6 个实时图表：消息吞吐量、客户端连接数、流量带宽、Topic 热力图、QoS 分布、错误率
- 支持 5/15/60 分钟时间范围切换
- 数据通过 SSE 每秒推送
- 使用 ECharts 渲染图表

**技术方案**：轻量级内存环形缓冲 + 自定义 MetricsCollector + SSE

---

## 数据模型

### 指标分类

| 类别 | 指标 | 类型 |
|------|------|------|
| 消息吞吐 | publish/s, receive/s | 速率（每秒增量） |
| 流量带宽 | bytesIn/s, bytesOut/s | 速率 |
| 客户端连接 | activeClients | 瞬时值 |
| QoS 分布 | qos0, qos1, qos2 | 累计计数 |
| Topic 热力 | top 10 topic 消息计数 | 累计计数 |
| 错误率 | errors/s | 速率（消息处理异常、认证失败、权限拒绝） |

### MetricSnapshot 结构

```java
public class MetricSnapshot {
    long timestamp;
    long publishDelta;
    long receiveDelta;
    long bytesInDelta;
    long bytesOutDelta;
    int activeClients;
    long qos0Delta;
    long qos1Delta;
    long qos2Delta;
    long errorsDelta;
    List<TopicCount> topTopics;  // top 10 [{topic, count}]
}
```

### 存储

- `ConcurrentLinkedDeque<MetricSnapshot>` 环形缓冲
- 最大 3600 条（1 小时 @ 1 条/秒）
- 超限自动淘汰最早的快照

---

## 后端设计

### 组件清单

| 组件 | 位置 | 职责 |
|------|------|------|
| `MetricsCollector` | `admin/.../dashboard/MetricsCollector.java` | 原子计数器、快照生成、环形缓冲 |
| `MetricsInterceptHandler` | `admin/.../dashboard/MetricsInterceptHandler.java` | 拦截 Moquette 事件，递增计数器 |
| `DashboardSseController` | `admin/.../dashboard/DashboardSseController.java` | SSE 端点，推送指标数据 |

### MetricsCollector

`@Service` 单例，包含：
- 原子计数器：`publishCount`, `receiveCount`, `bytesIn`, `bytesOut`, `qos0/1/2`, `errors`
- `ConcurrentHashMap<String, AtomicLong>` 记录每个 topic 消息计数
- `activeClients` 计数（通过 connect/disconnect 事件维护）
- `snapshot()` 方法：每秒调用，计算增量、构建 `MetricSnapshot`、写入环形缓冲

### MetricsInterceptHandler

`@Component InterceptHandler`，实现 Moquette 拦截接口：
- `onPublish()` → 递增 `publishCount`、`bytesIn`、topic 计数、QoS 计数
- `onConnect()` → 递增 `activeClients`
- `onDisconnect()` → 递减 `activeClients`
- 错误事件（消息处理异常、认证失败等） → 递增 `errors`

### DashboardSseController

- 端点：`GET /api/dashboard/stream`
- 参数：`range` = `5m` | `15m` | `60m`（默认 `15m`）
- 使用 `SseEmitter`，超时 `Long.MAX_VALUE`
- 每秒推送一次，从环形缓冲截取当前窗口数据
- 客户端断开时清理资源

### 推送 JSON 结构

```json
{
  "ts": 1688000000000,
  "publishRate": 120,
  "receiveRate": 85,
  "bytesInRate": 15360,
  "bytesOutRate": 10240,
  "activeClients": 42,
  "qos0": 1000,
  "qos1": 500,
  "qos2": 100,
  "errorRate": 2,
  "topTopics": [
    {"topic": "sensors/temp", "count": 300},
    {"topic": "sensors/humidity", "count": 200}
  ]
}
```

---

## 前端设计

### 页面布局

```
┌─────────────────────────────────────────────────┐
│  时间范围切换: [5分钟] [15分钟] [60分钟]           │
├─────────┬─────────┬─────────┬─────────┬─────────┤
│ 总消息  │ 活跃    │ 入站    │ 出站    │ 错误    │
│ 吞吐量  │ 客户端  │ 带宽    │ 带宽    │ 速率    │
├─────────┴─────────┴─────────┴─────────┴─────────┤
│  消息吞吐量（折线图）    │  客户端连接数（折线图）  │
├─────────────────────────┼───────────────────────┤
│  流量带宽（面积图）      │  Topic 热力图（柱状图）  │
├─────────────────────────┼───────────────────────┤
│  QoS 分布（饼图）        │  错误率（折线图）        │
└─────────────────────────┴───────────────────────┘
```

### 技术选型

- `vue-echarts` + `echarts` 包
- 折线图/面积图：`type: 'line'`，`areaStyle` 渐变
- 柱状图：`type: 'bar'`
- 饼图：`type: 'pie'`
- 响应式布局：2 列网格，小屏 1 列

### 数据流

- `EventSource` 连接 `/api/dashboard/stream?range=15m`
- 每次收到数据更新 ECharts `option`
- 切换时间范围时断开重连

### 保留内容

- 顶部 5 个概览卡片（替换原来的 MetricCards 组件）
- 下方 6 个图表替换原来的静态卡片（MqttBrokerCard、JvmMemoryCard 等）

---

## 依赖

- 后端：无新增依赖（使用 Spring SSE + 原子类）
- 前端：新增 `echarts` + `vue-echarts`（npm 包）

---

## 不做的事

- 不持久化指标数据（重启丢失，符合预期）
- 不对接 Prometheus/Grafana
- 不复用 `SseLogAppender`（独立 SSE 端点）
- 不实现自定义时间范围（仅 5/15/60 三档）
- 不新增 i18n key（图表标题复用现有翻译，如 `dashboard.title`）
