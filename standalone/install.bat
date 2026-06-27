@echo off
cd /d "%~dp0"
echo Installing mqtt-broker as Windows Service...
app.exe install
echo Starting mqtt-broker service...
app.exe start
echo Done.
pause
