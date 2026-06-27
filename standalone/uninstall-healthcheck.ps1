# uninstall-healthcheck.ps1 - 卸载健康检查计划任务

$taskName = "mqtt-broker-healthcheck"
Unregister-ScheduledTask -TaskName $taskName -Confirm:$false -ErrorAction SilentlyContinue
Write-Host "[SUCCESS] 计划任务 '$taskName' 已卸载" -ForegroundColor Green
