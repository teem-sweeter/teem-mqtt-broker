# Windows 下 Mosquitto MQTT Broker 安装部署完整文档

---

## 一、环境信息

| 项目 | 信息 |
|---|---|
| 操作系统 | Windows 11 |
| Mosquitto 版本 | 2.1.2 |
| 安装路径 | `C:\Program Files\Mosquitto` |

---

## 二、安装 Mosquitto

### 2.1 下载安装

前往 Mosquitto 官网下载 Windows 64-bit 安装包：

```
https://mosquitto.org/download/
```

双击安装，默认安装路径为 `C:\Program Files\Mosquitto\`。

### 2.2 验证安装

打开 PowerShell，执行：

```powershell
cd "C:\Program Files\Mosquitto"
.\mosquitto.exe -h
```

确认输出显示 `mosquitto version 2.1.2` 表示安装成功。

---

## 三、配置文件

### 3.1 编辑配置文件

> **注意：** `C:\Program Files\` 是系统保护目录，需要用管理员权限编辑。

**方式一：管理员权限记事本**
1. 开始菜单搜索「记事本」
2. 右键 → 以管理员身份运行
3. 文件 → 打开 → 导航到 `C:\Program Files\Mosquitto\`
4. 文件类型选「所有文件 (*.*)」
5. 找到 `mosquitto.conf` 打开编辑

**方式二：管理员 PowerShell**
```powershell
notepad "C:\Program Files\Mosquitto\mosquitto.conf"
```

### 3.2 生产环境配置（在配置文件末尾添加）

```conf
# ============ 监听器 ============

# 普通 MQTT 端口
listener 1883
protocol mqtt

# WebSocket 端口（用于浏览器连接）
listener 9001
protocol websockets

# Web Dashboard 端口
listener 8080
protocol http_api
http_dir dashboard

# ============ 安全 ============

# 禁止匿名连接
allow_anonymous false

# 密码文件（后续创建）
password_file pwfile

# ============ 持久化 ============

persistence true
persistence_file mosquitto.db
autosave_interval 1800

# ============ 资源限制 ============

max_connections 10000
max_queued_messages 1000

# ============ 日志 ============

log_dest stdout
log_dest file mosquitto.log
log_type error
log_type warning
log_type notice
log_timestamp true
log_timestamp_format %Y-%m-%dT%H:%M:%S
connection_messages true
```

### 3.3 开发环境简化配置

如果只是本地测试，可以简化为：

```conf
# MQTT 端口
listener 1883
protocol mqtt

# WebSocket 端口
listener 9001
protocol websockets

# Web Dashboard
listener 8080
protocol http_api
http_dir dashboard

# 允许匿名
allow_anonymous true

# 日志
log_dest stdout
log_type all
```

---

## 四、创建密码文件

### 4.1 生成密码文件

```powershell
cd "C:\Program Files\Mosquitto"
.\mosquitto_passwd.exe -c pwfile admin
```

按提示输入两次密码，会生成 `pwfile` 文件。

### 4.2 添加更多用户

```powershell
# 注意：不加 -c，否则会覆盖原文件
.\mosquitto_passwd.exe pwfile user2
.\mosquitto_passwd.exe pwfile user3
```

### 4.3 创建 ACL 文件（可选）

创建文件 `aclfile`，写入访问控制规则：

```
# 所有用户可读写所有主题（开发环境）
topic readwrite #
```

创建方式（管理员 PowerShell）：

```powershell
New-Item -Path "C:\Program Files\Mosquitto\aclfile" -ItemType File -Force
notepad "C:\Program Files\Mosquitto\aclfile"
```

---

## 五、手动启动与测试

### 5.1 前台启动

```powershell
cd "C:\Program Files\Mosquitto"
.\mosquitto.exe -c mosquitto.conf -v
```

正常输出应包含：

```
Opening ipv4 listen socket on port 1883.
Opening ipv4 listen socket on port 9001.
Opening http api listen socket on port 8080.
Using http_dir C:\Program Files\Mosquitto\dashboard\
mosquitto version 2.1.2 running
```

### 5.2 访问 Web Dashboard

浏览器打开：

```
http://localhost:8080
```

### 5.3 WebSocket 连接测试

打开浏览器访问以下任一 Web MQTT 客户端：

| 工具 | 地址 |
|---|---|
| MQTTX Web | https://mqttx.app/web-app |
| HiveMQ WebSocket Client | https://www.hivemq.com/demos/websocket-client/ |

连接信息：

```
Host: localhost
Port: 9001
Username: admin
Password: 你设置的密码
```

### 5.4 命令行测试（另开终端）

```powershell
# 订阅
.\mosquitto_sub.exe -t "test/topic" -v -u admin -P 你的密码

# 发布（再开一个终端）
.\mosquitto_pub.exe -t "test/topic" -m "Hello MQTT" -u admin -P 你的密码
```

---

## 六、开机自启动

### 6.1 原因说明

直接将 Mosquitto 注册为 Windows 服务会因为 `C:\Program Files\` 路径中的空格导致参数解析错误，服务启动后立刻退出（错误码 13）。

### 6.2 解决方案：复制到无空格路径

```powershell
# 创建无空格目录
New-Item -Path "C:\mosquitto" -ItemType Directory -Force

# 复制所有文件
Copy-Item "C:\Program Files\Mosquitto\*" -Destination "C:\mosquitto\" -Recurse -Force
```

### 6.3 通过任务计划程序创建开机自启

```powershell
# 停掉已有的 Mosquitto 进程
Stop-Process -Name mosquitto -Force -ErrorAction SilentlyContinue
Start-Sleep 1

# 删除旧任务（如果存在）
schtasks.exe /delete /tn "Mosquitto" /f 2>$null

# 创建任务（SYSTEM 账户，开机自动启动）
schtasks.exe /create /tn "Mosquitto" /tr "C:\mosquitto\mosquitto.exe -c C:\mosquitto\mosquitto.conf" /sc onstart /rl highest /ru SYSTEM /f
```

### 6.4 手动启动任务

```powershell
schtasks.exe /run /tn "Mosquitto"
Start-Sleep 3
```

### 6.5 验证

```powershell
# 查看进程是否运行
Get-Process mosquitto -ErrorAction SilentlyContinue

# 查看任务状态
schtasks.exe /query /tn "Mosquitto" /v /fo list | findstr "Status Last Result Run As User"
```

正常输出：

```
Handles  NPM(K)  PM(K)  WS(K)  CPU(s)     Id  ProcessName
-------  ------  -----  -----  ------     --  -----------
   ...                                     ...  mosquitto
```

---

## 七、日常管理命令

| 操作 | 命令 |
|---|---|
| 手动启动 | `schtasks.exe /run /tn "Mosquitto"` |
| 停止进程 | `Stop-Process -Name mosquitto -Force` |
| 查看进程状态 | `Get-Process mosquitto -ErrorAction SilentlyContinue` |
| 查看任务状态 | `schtasks.exe /query /tn "Mosquitto" /v /fo list` |
| 查看监听端口 | `netstat -ano \| findstr "1883 9001 8080"` |
| 删除自启任务 | `schtasks.exe /delete /tn "Mosquitto" /f` |
| 查看日志 | 打开 `C:\mosquitto\mosquitto.log` |

---

## 八、目录结构说明

```
C:\mosquitto\                          ← 运行目录（无空格路径）
├── mosquitto.conf                     ← 配置文件
├── mosquitto.exe                      ← 主程序
├── mosquitto_passwd.exe               ← 密码工具
├── mosquitto_sub.exe                  ← 命令行订阅工具
├── mosquitto_pub.exe                  ← 命令行发布工具
├── pwfile                             ← 密码文件
├── aclfile                            ← ACL 文件（可选）
├── mosquitto.db                       ← 持久化数据库
├── mosquitto.log                      ← 运行日志
└── dashboard/                         ← Web 管理界面
    ├── index.html
    ├── listeners.html
    ├── app/
    ├── css/
    ├── lib/
    ├── media/
    ├── tailwind/
    └── utils/
```

---

## 九、端口说明

| 端口 | 协议 | 用途 |
|---|---|---|
| 1883 | MQTT | 标准 MQTT 连接（设备、代码客户端） |
| 9001 | WebSocket | 浏览器 MQTT 客户端连接 |
| 8080 | HTTP API | Web Dashboard 管理界面 |

---

## 十、常见问题排查

| 问题 | 原因 | 解决 |
|---|---|---|
| `password-file: Error: Unable to open pwfile` | 密码文件不存在 | 运行 `mosquitto_passwd.exe -c pwfile admin` 创建 |
| `Error: Unable to open acl_file` | ACL 文件不存在 | 创建空 ACL 文件或注释掉 `acl_file` 配置 |
| 服务启动后立刻停止 | 路径有空格导致参数解析错误 | 复制到 `C:\mosquitto\` 并使用任务计划程序 |
| `http_dir` 路径拼接重复 | 绝对路径被当相对路径处理 | 使用相对路径 `http_dir dashboard` |
| 端口被占用 | 上一个 Mosquitto 进程未完全退出 | `netstat -ano \| findstr 1883` 找到 PID 并 `taskkill /PID xxx /F` |
| 任务计划错误码 13 | 参数解析失败 | 使用 `schtasks.exe` 创建 + SYSTEM 账户 + 无空格路径 |