@echo off
cd /d "%~dp0"
echo Stopping mqtt-broker Service...
app.exe stop
echo Starting mqtt-broker Service...
app.exe start
echo Service restarted successfully.
pause
