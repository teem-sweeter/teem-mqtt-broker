# Standalone 部署包

Windows 独立部署包，内置 JDK，开箱即用。

## 目录结构

```
standalone/
├── app.jar                      # 应用程序
├── app.exe                      # Windows 服务封装（WinSW）
├── app.xml                      # 服务配置
├── app.bat                      # 快速重启脚本
├── install.bat                  # 安装为 Windows 服务
├── jdk/                         # 内置 JDK 17
├── manager.ps1                  # 服务管理脚本
├── healthcheck.ps1              # 健康检查脚本
├── install-healthcheck.ps1      # 注册健康检查计划任务
└── uninstall-healthcheck.ps1    # 卸载健康检查计划任务
```

## 快速开始

### 方式一：直接运行

```powershell
# 使用管理脚本启动
.\manager.ps1 -Action start

# 查看状态
.\manager.ps1 -Action status

# 停止
.\manager.ps1 -Action stop

# 重启
.\manager.ps1 -Action restart
```

### 方式二：安装为 Windows 服务

以管理员身份运行：

```cmd
install.bat
```

安装后服务名称为 `mqtt-broker`，开机自动启动。可通过 `services.msc` 管理。

## 健康检查

健康检查脚本每 2 分钟检测一次应用状态：
- 检查进程是否存在
- 检查 `/actuator/health` 端点是否正常
- 连续失败 3 次自动重启服务

### 注册健康检查（管理员权限）

```powershell
.\install-healthcheck.ps1
```

### 卸载健康检查

```powershell
.\uninstall-healthcheck.ps1
```

### 查看健康检查日志

```powershell
Get-Content .\logs\healthcheck.log -Tail 50
```

## 配置说明

### JVM 参数

在 `app.xml` 中修改（Windows 服务模式）：

```xml
<arguments>
    -Xms1G          # 初始堆内存
    -Xmx1G          # 最大堆内存
    -Dfile.encoding=UTF-8
    -jar app.jar
</arguments>
```

或在 `manager.ps1` 中修改（脚本模式）：

```powershell
$JAVA_OPTS = @(
    "-Xms1G"
    "-Xmx1G"
    "-Dfile.encoding=UTF-8"
)
```

### 应用配置

编辑 `application.yml`（首次启动后自动生成在当前目录），或通过环境变量覆盖：

```powershell
$env:JWT_SECRET = "your-secret-key"
$env:MQTT_BROKER_PASSWORD = "your-password"
```

## 日志文件

| 文件 | 说明 |
|---|---|
| `logs/application.log` | 应用标准输出 |
| `logs/application-error.log` | 应用错误输出 |
| `logs/healthcheck.log` | 健康检查日志 |
| `logs/app.log` | 应用内部日志 |
| `logs/info.log` | INFO 级别日志 |
| `logs/error.log` | ERROR 级别日志 |

## 访问地址

启动后默认访问：

| 服务 | 地址 |
|---|---|
| Web 管理控制台 | http://localhost:8080 |
| MQTT TCP | `localhost:1883` |
| MQTT WebSocket | `ws://localhost:8083/mqtt` |
| API 文档 | http://localhost:8080/doc.html |
