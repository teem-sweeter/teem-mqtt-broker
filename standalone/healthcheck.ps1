# healthcheck.ps1 - auto restart on health check failure
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$ScriptDir = $PSScriptRoot
$HEALTH_URL = "http://127.0.0.1:8080/actuator/health"
$TIMEOUT_SEC = 10
$MAX_FAILURES = 3
$FAILURE_FILE = Join-Path $ScriptDir "healthcheck.failures"
$LOG_FILE = Join-Path $ScriptDir "logs\healthcheck.log"

$logDir = Split-Path $LOG_FILE
if (-not (Test-Path $logDir)) {
    New-Item -ItemType Directory -Path $logDir | Out-Null
}

function Write-Log {
    param([string]$Message)
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    "$timestamp $Message" | Out-File $LOG_FILE -Append -Encoding utf8
}

function Get-FailureCount {
    if (Test-Path $FAILURE_FILE) {
        $count = (Get-Content $FAILURE_FILE -Raw).Trim()
        if ($count -match '^\d+$') {
            return [int]$count
        }
    }
    return 0
}

function Set-FailureCount {
    param([int]$Count)
    $Count | Out-File $FAILURE_FILE -Encoding ascii -NoNewline
}

function Clear-Failures {
    Remove-Item $FAILURE_FILE -ErrorAction SilentlyContinue
}

function Test-ProcessAlive {
    $pidFile = Join-Path $ScriptDir "application.pid"
    if (-not (Test-Path $pidFile)) {
        return $false
    }
    $appPid = (Get-Content $pidFile -Raw).Trim()
    if (-not $appPid) { return $false }
    try {
        $proc = Get-Process -Id $appPid -ErrorAction Stop
        $cmd = (Get-CimInstance Win32_Process -Filter "ProcessId=$appPid").CommandLine
        return ($cmd -and $cmd.Contains("app.jar"))
    } catch {
        return $false
    }
}

function Test-HealthEndpoint {
    try {
        $response = Invoke-WebRequest -Uri $HEALTH_URL -TimeoutSec $TIMEOUT_SEC -UseBasicParsing -ErrorAction Stop
        return ($response.StatusCode -eq 200)
    } catch {
        return $false
    }
}

function Restart-App {
    Write-Log "[RESTART] restarting service..."
    & powershell -ExecutionPolicy Bypass -File (Join-Path $ScriptDir "manager.ps1") -Action stop 2>&1 | ForEach-Object { Write-Log $_ }
    Start-Sleep -Seconds 3
    & powershell -ExecutionPolicy Bypass -File (Join-Path $ScriptDir "manager.ps1") -Action start 2>&1 | ForEach-Object { Write-Log $_ }
    Start-Sleep -Seconds 10
    if (Test-HealthEndpoint) {
        Write-Log "[RESTART] restart success, health check passed"
        Clear-Failures
    } else {
        Write-Log "[RESTART] health check still failing after restart"
    }
}

# main
if (-not (Test-ProcessAlive)) {
    Write-Log "[CHECK] process not found, starting..."
    & powershell -ExecutionPolicy Bypass -File (Join-Path $ScriptDir "manager.ps1") -Action start 2>&1 | ForEach-Object { Write-Log $_ }
    Clear-Failures
    exit 0
}

if (Test-HealthEndpoint) {
    Clear-Failures
    exit 0
}

$failures = Get-FailureCount + 1
Set-FailureCount $failures
Write-Log "[CHECK] health check failed ($failures/$MAX_FAILURES)"

if ($failures -ge $MAX_FAILURES) {
    Write-Log "[CHECK] failed $MAX_FAILURES times, triggering restart"
    Restart-App
}
