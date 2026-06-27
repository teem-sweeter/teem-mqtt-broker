# manager.ps1
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("start", "stop", "restart", "status")]
    [string]$Action
)

# =========================
# 基础配置
# =========================

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$ScriptDir = $PSScriptRoot

$JAR_NAME = "app.jar"

$JAR_PATH = Join-Path $ScriptDir $JAR_NAME

$LOG_DIR = Join-Path $ScriptDir "logs"

$LOG_FILE = Join-Path $LOG_DIR "application.log"

$PID_FILE = Join-Path $ScriptDir "application.pid"

# JVM 参数
$JAVA_OPTS = @(
    "-Xms1G"
    "-Xmx1G"
    "-Dfile.encoding=UTF-8"
)

# =========================
# 初始化目录
# =========================

if (-not (Test-Path $LOG_DIR)) {
    New-Item -ItemType Directory -Path $LOG_DIR | Out-Null
}

# =========================
# 获取 PID
# =========================

function Get-AppPid {

    if (-not (Test-Path $PID_FILE)) {
        return $null
    }

    $raw = Get-Content $PID_FILE -Raw -ErrorAction SilentlyContinue
    if (-not $raw) {
        Remove-Item $PID_FILE -ErrorAction SilentlyContinue
        return $null
    }

    $AppPid = $raw.Trim()

    if (-not $AppPid) {
        Remove-Item $PID_FILE -ErrorAction SilentlyContinue
        return $null
    }

    try {

        $proc = Get-Process -Id $AppPid -ErrorAction Stop

        # 校验是否是当前 jar
        $cmd = (Get-CimInstance Win32_Process -Filter "ProcessId=$AppPid").CommandLine

        if ($cmd -and $cmd.Contains($JAR_NAME)) {
            return $AppPid
        }

    } catch {
    }

    Remove-Item $PID_FILE -ErrorAction SilentlyContinue

    return $null
}

# =========================
# 启动服务
# =========================

function Start-Service {

    $AppPid = Get-AppPid

    if ($AppPid) {
        Write-Host "[INFO] 服务已运行 (PID: $AppPid)" -ForegroundColor Cyan
        return
    }

    if (-not (Test-Path $JAR_PATH)) {
        Write-Host "[ERROR] 未找到 Jar 文件: $JAR_PATH" -ForegroundColor Red
        exit 1
    }

    # 优先使用内置 JDK
    $bundledJava = Join-Path $ScriptDir "jdk\bin\java.exe"
    if (Test-Path $bundledJava) {
        $javaExe = $bundledJava
    } else {
        $javaCmd = Get-Command java -ErrorAction SilentlyContinue
        if (-not $javaCmd) {
            Write-Host "[ERROR] 未找到 Java，请检查 JAVA_HOME 或 PATH" -ForegroundColor Red
            exit 1
        }
        $javaExe = "java"
    }

    Write-Host "[INFO] 正在启动服务..." -ForegroundColor Yellow

    $arguments = @()

    $arguments += $JAVA_OPTS

    $arguments += "-jar"

    $arguments += $JAR_PATH

    $stderrFile = Join-Path $LOG_DIR "application-error.log"

    $process = Start-Process `
        -FilePath $javaExe `
        -ArgumentList $arguments `
        -RedirectStandardOutput $LOG_FILE `
        -RedirectStandardError $stderrFile `
        -WorkingDirectory $ScriptDir `
        -WindowStyle Hidden `
        -PassThru

    Start-Sleep -Seconds 3

    if ($process.HasExited) {

        Write-Host "[ERROR] 启动失败，请检查日志：" -ForegroundColor Red
        Write-Host "$LOG_FILE"

        exit 1
    }

    $process.Id | Out-File $PID_FILE -Encoding ascii -NoNewline

    Write-Host "[SUCCESS] 启动成功 (PID: $($process.Id))" -ForegroundColor Green
    Write-Host "[INFO] 日志文件: $LOG_FILE"
}

# =========================
# 停止服务
# =========================

function Stop-Service {

    $AppPid = Get-AppPid

    if (-not $AppPid) {
        Write-Host "[WARN] 服务未运行" -ForegroundColor Yellow
        return
    }

    Write-Host "[INFO] 正在停止服务 (PID: $AppPid)..." -ForegroundColor Yellow

    try {

        Stop-Process -Id $AppPid -ErrorAction Stop

        $timeout = 15

        while ($timeout -gt 0) {

            Start-Sleep -Seconds 1

            $proc = Get-Process -Id $AppPid -ErrorAction SilentlyContinue

            if (-not $proc) {
                break
            }

            $timeout--
        }

        if ($proc) {

            Write-Host "[WARN] 进程未正常退出，强制结束..." -ForegroundColor Yellow

            Stop-Process -Id $AppPid -Force
        }

        Write-Host "[SUCCESS] 服务已停止" -ForegroundColor Green

    } catch {

        Write-Host "[ERROR] 停止失败: $_" -ForegroundColor Red
    }

    Remove-Item $PID_FILE -ErrorAction SilentlyContinue
}

# =========================
# 查看状态
# =========================

function Status-Service {

    $AppPid = Get-AppPid

    if ($AppPid) {

        $proc = Get-Process -Id $AppPid

        Write-Host "[STATUS] 运行中" -ForegroundColor Green
        Write-Host "PID      : $AppPid"
        Write-Host "内存(MB) : $([math]::Round($proc.WorkingSet64 / 1MB, 2))"
        Write-Host "启动时间 : $($proc.StartTime)"

    } else {

        Write-Host "[STATUS] 未运行" -ForegroundColor Red
    }
}

# =========================
# 主入口
# =========================

switch ($Action) {

    "start" {

        Start-Service
    }

    "stop" {

        Stop-Service
    }

    "restart" {

        Stop-Service

        Start-Sleep -Seconds 3

        Start-Service
    }

    "status" {

        Status-Service
    }
}
