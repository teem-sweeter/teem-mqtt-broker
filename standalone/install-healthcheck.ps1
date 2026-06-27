# install-healthcheck.ps1 - 注册健康检查计划任务
# 以管理员权限运行

$taskName = "mqtt-broker-healthcheck"
$scriptPath = Join-Path $PSScriptRoot "healthcheck.ps1"

# 删除已有任务
Unregister-ScheduledTask -TaskName $taskName -Confirm:$false -ErrorAction SilentlyContinue

# 创建任务触发器：每 2 分钟执行一次
$trigger = New-ScheduledTaskTrigger -Once -At (Get-Date) -RepetitionInterval (New-TimeSpan -Minutes 2)

# 创建任务操作
$action = New-ScheduledTaskAction -Execute "powershell.exe" -Argument "-ExecutionPolicy Bypass -WindowStyle Hidden -File `"$scriptPath`""

# 创建任务设置
$settings = New-ScheduledTaskSettingsSet `
    -AllowStartIfOnBatteries `
    -DontStopIfGoingOnBatteries `
    -StartWhenAvailable `
    -ExecutionTimeLimit (New-TimeSpan -Minutes 5)

# 注册任务
Register-ScheduledTask `
    -TaskName $taskName `
    -Trigger $trigger `
    -Action $action `
    -Settings $settings `
    -Description "MQTT Broker 健康检查，检测假死自动重启" `
    -RunLevel Highest

Write-Host "[SUCCESS] 计划任务 '$taskName' 已注册（每 2 分钟检查一次）" -ForegroundColor Green
Write-Host "[INFO] 卸载命令: Unregister-ScheduledTask -TaskName '$taskName' -Confirm:`$false"
