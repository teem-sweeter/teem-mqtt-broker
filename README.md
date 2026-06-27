# Teem MQTT Broker

基于 Spring Boot + Moquette 的 MQTT Broker，内置 Web 管理控制台，提供实时监控、消息持久化、日志管理和在线调试等功能。

## 功能特性

### MQTT Broker

- 基于 [Moquette](https://github.com/moquette-io/moquette) 0.18.0 引擎
- 支持 MQTT 3.1 / 3.1.1 / 5.0 协议
- TCP (`1883`) 和 WebSocket (`8083`) 双接入方式
- 用户名密码认证，可配置匿名访问
- 重复 ClientID 策略：拒绝新连接或断开旧连接
- 消息持久化存储（H2 嵌入式数据库，AES 加密）

### Web 管理控制台

- **仪表盘** — 系统总览：JVM 内存、线程、GC、CPU、磁盘等运行指标
- **MQTT 监控** — 实时 WebSocket 推送：连接数、消息速率、订阅统计、在线客户端列表、Topic 统计
- **客户端管理** — 在线客户端详情、强制断开、订阅管理
- **消息记录** — 按 Topic / 客户端 / 时间范围检索历史消息，支持趋势图，数据量可配置上限
- **MQTT 调试工具** — 内置 MQTT 客户端，支持订阅、发布、QoS 选择、Retain 标记
- **日志管理** — 按级别搜索日志文件，SSE 实时日志流（终端风格，按级别着色）
- **系统监控** — Spring Actuator 集成
- **在线升级** — 上传签名升级包，自动校验 RSA 签名并热重启

### 安全

- JWT 认证（Web API + WebSocket）
- 升级包 RSA-2048 签名校验
- H2 数据库 AES 文件级加密

## 技术栈

| 层 | 技术 |
|---|---|
| 后端框架 | Spring Boot 4.0.6 |
| MQTT 引擎 | Moquette 0.18.0 |
| 数据库 | H2（嵌入式，AES 加密） |
| ORM | Spring Data JPA / Hibernate |
| 认证 | JWT (jjwt 0.12.6) |
| API 文档 | Knife4j 4.5.0 (OpenAPI 3) |
| 前端框架 | Vue 3.5 + Vue Router 4 + Pinia 2 |
| UI 组件库 | Element Plus 2.11 |
| 构建工具 | Maven + Vite |

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.9+
- Node.js 20+（构建前端时需要，`frontend-maven-plugin` 会自动下载）

### 构建

```bash
# 完整构建（包含前端）
mvn clean install

# 跳过测试
mvn clean package -DskipTests

# 仅构建 admin 模块
mvn clean install -pl admin -am
```

### 运行

```bash
java -jar admin/target/admin-1.0.0.jar
```

启动后访问：

| 服务 | 地址 |
|---|---|
| Web 管理控制台 | http://localhost:8080 |
| MQTT TCP | `localhost:1883` |
| MQTT WebSocket | `ws://localhost:8083/mqtt` |
| API 文档 (Swagger) | http://localhost:8080/doc.html |

默认登录凭据：`admin` / `admin2026`

## 项目结构

```
mqtt-broker/
├── mqtt-broker/                  # MQTT Broker 核心模块
│   └── src/main/java/com/jjc/mqtt/
│       ├── MoquetteAutoConfiguration.java    # Spring Boot 自动配置
│       ├── MoquetteProperties.java           # Broker 配置属性
│       ├── MqttPersistenceProperties.java    # 持久化配置属性
│       ├── DefaultMoquetteAuthenticator.java # 认证实现
│       ├── ConnectedClients.java             # 在线客户端管理
│       ├── handler/          # 拦截处理器（监控、持久化、桥接）
│       ├── monitor/          # 监控服务与数据模型
│       ├── bridge/           # 桥接引擎与路由
│       └── event/            # MQTT 消息事件
│
├── admin/                      # Spring Boot 主应用
│   └── src/main/java/com/jjc/mqtt/admin/
│       ├── controller/         # REST API（认证、监控、健康检查）
│       ├── persistence/        # 消息持久化（JPA + H2）
│       ├── security/           # JWT 认证与鉴权
│       ├── websocket/          # WebSocket 实时推送
│       ├── logging/            # 日志管理与 SSE 流
│       ├── bridge/             # 桥接管理（REST API + 持久化）
│       ├── client/             # 客户端规则管理
│       ├── upgrade/            # 在线升级与签名校验
│       ├── actuator/           # 自定义 Actuator 端点
│       └── configuration/      # Web / WebSocket / Knife4j 配置
│
├── ui-webjar/                  # 前端 Web UI（Vue 3 + Element Plus）
│   └── src/
│       ├── views/              # 页面组件
│       ├── api/                # API 请求封装
│       └── router/             # 路由配置
│
└── maven-jar-sign-plugin/      # JAR 签名 Maven 插件
```

## 配置说明

### MQTT Broker

在 `application.yml` 中配置：

```yaml
mqtt:
  broker:
    enabled: true
    host: 0.0.0.0
    port: 1883                  # TCP 端口
    websocket-port: 8083        # WebSocket 端口
    websocket-path: /mqtt       # WebSocket 路径
    allow-anonymous: false      # 是否允许匿名连接
    username: admin             # MQTT 用户名
    password: admin2026         # MQTT 密码
    duplicate-client-id-strategy: REJECT_NEW  # REJECT_NEW 或 DISCONNECT_OLD
```

### 消息持久化

```yaml
mqtt:
  broker:
    persistence:
      enabled: true
      queue-size: 10000         # 异步队列大小
      max-rows: 10000           # 数据库最大行数（超出时自动清理最旧记录）
      log-path: ./logs/mqtt     # 日志存储路径
      max-file-size: 100MB      # 单文件最大大小
      max-history: 30           # 保留天数
      total-size-cap: 20GB      # 总大小上限
```

### JWT 认证

```yaml
jwt:
  secret: ${JWT_SECRET:mqtt-edge-default-secret-key-must-be-at-least-32bytes}
  expiration: 86400000          # Token 有效期（毫秒，默认 24 小时）
```

## API 接口

启动后访问 http://localhost:8080/doc.html 查看完整的 Swagger API 文档。

主要接口：

| 方法 | 路径 | 说明 |
|---|---|---|
| `POST` | `/v1/login` | 登录，返回 JWT Token |
| `GET` | `/v1/health` | 系统健康检查 |
| `GET` | `/v1/monitor/stats` | MQTT Broker 统计信息 |
| `GET` | `/v1/monitor/clients` | 在线客户端列表 |
| `POST` | `/v1/monitor/publish` | 通过 Broker 发布消息 |
| `GET` | `/v1/messages` | 分页查询持久化消息 |
| `GET` | `/v1/messages/stats` | 消息统计 |
| `GET` | `/v1/messages/trend` | 消息趋势 |
| `GET` | `/v1/logs/sse` | 实时日志流 (SSE) |
| `WS` | `/ws/monitor` | WebSocket 实时监控 |

## 在线升级

系统支持通过 Web 界面上传签名升级包（`.zip`）进行在线升级：

1. 使用 `maven-jar-sign-plugin` 对构建产物签名
2. 将 JAR 和签名文件打包为 `.zip`
3. 在「系统升级」页面上传
4. 系统自动校验 RSA 签名、备份旧版本并重启

详见 [更新包密匙生成指南](docs/更新包密匙生成指南.md)。

## 许可证

本项目基于 [Apache License 2.0](LICENSE) 开源。
