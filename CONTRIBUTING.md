# 贡献指南

感谢你对 Teem MQTT Broker 项目的关注！我们欢迎各种形式的贡献。

## 如何贡献

### 报告 Bug

1. 在 [Issues](../../issues) 中搜索是否已有相同问题
2. 如果没有，使用 Bug Report 模板创建新 Issue
3. 请提供尽可能详细的信息：复现步骤、期望行为、实际行为、环境信息

### 提交功能建议

1. 在 [Issues](../../issues) 中使用 Feature Request 模板创建新 Issue
2. 描述你的使用场景和期望的功能

### 提交代码

1. Fork 本仓库
2. 创建你的特性分支：`git checkout -b feature/my-feature`
3. 提交你的修改：`git commit -m 'feat: 添加某功能'`
4. 推送到分支：`git push origin feature/my-feature`
5. 创建 Pull Request

## 开发环境

### 环境要求

- JDK 17+
- Maven 3.9+
- Node.js 20+（前端构建，`frontend-maven-plugin` 会自动下载）

### 构建项目

```bash
mvn clean install
```

### 运行项目

```bash
java -jar admin/target/admin-1.0.0.jar
```

## 代码规范

### Commit 消息格式

遵循 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

```
<type>(<scope>): <subject>

<body>

<footer>
```

类型（type）：
- `feat`: 新功能
- `fix`: Bug 修复
- `docs`: 文档变更
- `style`: 代码格式（不影响代码运行的变更）
- `refactor`: 重构（既不是新功能也不是修复）
- `perf`: 性能优化
- `test`: 增加测试
- `chore`: 构建过程或辅助工具的变更

### 代码风格

- 使用 4 空格缩进
- 遵循 Java 编码规范
- 公共方法必须添加 Javadoc
- 保持代码简洁，避免不必要的注释

## Pull Request 规范

- 确保 PR 描述清晰说明了改动内容和原因
- 关联相关的 Issue
- 确保代码能通过编译：`mvn clean compile -DskipTests`
- 保持 PR 范围聚焦，一个 PR 只解决一个问题

## 行为准则

参与本项目即表示你同意遵守我们的 [行为准则](CODE_OF_CONDUCT.md)。
