# 更新日志

本项目遵循 [语义化版本控制](https://semver.org/lang/zh-CN/)。

## [1.0.0] - 2026-06-27

首个开源版本。

### MQTT Broker

- 基于 Moquette 0.18.0，支持 MQTT 3.1 / 3.1.1 / 5.0
- TCP (1883) 和 WebSocket (8083) 双接入
- 用户名密码认证，可配置匿名访问
- 重复 ClientID 策略：拒绝新连接或断开旧连接

### Web 管理控制台

- 仪表盘：JVM 内存、线程、GC、CPU、磁盘等运行指标
- MQTT 监控：实时 WebSocket 推送，连接数、消息速率、订阅统计
- 客户端管理：在线客户端详情、强制断开、订阅管理
- 消息记录：按 Topic / 客户端 / 时间范围检索，趋势图，数据量可配置上限
- MQTT 调试工具：内置客户端，支持订阅、发布、QoS 选择
- 日志管理：按级别搜索，SSE 实时日志流
- 在线升级：RSA 签名校验，自动备份与热重启

### 桥接功能

- 支持多 Broker 桥接连接
- 灵活的 Topic 路由规则
- 连接测试与状态监控

### 安全

- JWT 认证（Web API + WebSocket）
- 升级包 RSA-2048 签名校验
- H2 数据库 AES 文件级加密

### 部署

- Windows 服务部署（内置 JDK，WinSW 封装）
- 健康检查与自动重启（计划任务）
- 服务管理脚本（start / stop / restart / status）
