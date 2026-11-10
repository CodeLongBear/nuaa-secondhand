@echo off
setlocal
chcp 65001 >nul

set "ROOT=%~dp0"
set "BACKEND=%ROOT%backend"
set "FRONTEND=%ROOT%frontend"
set "REDIS_DIR=%ROOT%tools\redis"

echo ==================================================
echo   校园二手交易平台  一键启动
echo ==================================================
echo.

REM ---- 1. 工具链检查 ----
set "MISSING="
where java >nul 2>nul || set "MISSING=%MISSING% JDK17"
where mvn  >nul 2>nul || set "MISSING=%MISSING% Maven"
where node >nul 2>nul || set "MISSING=%MISSING% Node"

if defined MISSING (
    echo [缺失] 请先安装并加入 PATH:%MISSING%
    echo.
    pause
    exit /b 1
)
echo [OK]  工具链齐备（Java / Maven / Node）
echo.

REM ---- 2. 依赖服务检查（仅提示，不阻断）----
netstat -ano | findstr ":3306" | findstr "LISTENING" >nul 2>nul && (echo [OK]  MySQL 3306) || (echo [警告] MySQL 3306 未运行，请先启动)
netstat -ano | findstr ":5672" | findstr "LISTENING" >nul 2>nul && (echo [OK]  RabbitMQ 5672) || (echo [警告] RabbitMQ 5672 未运行，秒杀功能不可用)
echo.

REM ---- 3. 前端依赖（首次自动安装）----
if not exist "%FRONTEND%\node_modules" (
    echo [首次] 未检测到前端依赖，正在 npm install，请稍候...
    pushd "%FRONTEND%"
    call npm install
    popd
)

REM ---- 4. 启动 Redis ----
netstat -ano | findstr ":6379" | findstr "LISTENING" >nul 2>nul
if errorlevel 1 (
    echo [启动] Redis     6379
    start "Redis" /d "%REDIS_DIR%" redis-server.exe
) else (
    echo [跳过] Redis 已在运行
)

REM ---- 5. 启动后端 ----
netstat -ano | findstr ":8081" | findstr "LISTENING" >nul 2>nul
if errorlevel 1 (
    echo [启动] 后端      8081  ^(profile=local^)
    start "Backend" /d "%BACKEND%" cmd /k "mvn spring-boot:run -Dspring-boot.run.profiles=local"
) else (
    echo [跳过] 后端已在运行
)

REM ---- 6. 启动前端 ----
netstat -ano | findstr ":5173" | findstr "LISTENING" >nul 2>nul
if errorlevel 1 (
    echo [启动] 前端      5173
    start "Frontend" /d "%FRONTEND%" cmd /k "npm run dev"
) else (
    echo [跳过] 前端已在运行
)

echo.
echo ==================================================
echo   后端 http://localhost:8081
echo   前端 http://localhost:5173  ^(浏览器打开^)
echo   登录学号 20210001（验证码开发模式明文显示）
echo   停止服务请运行 stop.bat
echo ==================================================
echo.
pause
