@echo off
setlocal
chcp 65001 >nul

echo ==================================================
echo   校园二手交易平台  停止服务
echo ==================================================
echo.

for %%p in (6379 8081 5173) do (
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%%p" ^| findstr "LISTENING"') do (
        echo [关闭] 端口 %%p  PID %%a
        taskkill /f /pid %%a >nul 2>nul
    )
)

echo.
echo 已停止。若个别进程仍在，可重复运行本脚本。
echo.
pause
