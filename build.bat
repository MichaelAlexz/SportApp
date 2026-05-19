@echo off
chcp 65001 >nul
title 轻动 APP - 构建工具

echo =======================================
echo   轻动运动APP - 构建助手
echo =======================================
echo.
echo 当前网络环境:
echo   [1] 国内网络（使用阿里云镜像）
echo   [2] 海外网络（使用官方源）
echo.
set /p choice="请选择网络模式 (1/2，默认1): "
if "%choice%"=="" set choice=1

if "%choice%"=="1" (
    echo.
    echo [✓] 已选择国内镜像模式
    echo  Gradle: 腾讯云镜像
    echo  Maven:  阿里云镜像
    set DIST_URL=https://mirrors.cloud.tencent.com/gradle/gradle-8.5-bin.zip
) else (
    echo.
    echo [✓] 已选择海外官方模式
    set DIST_URL=https://services.gradle.org/distributions/gradle-8.5-bin.zip
)

echo.
echo =======================================
echo   开始编译...
echo =======================================
echo.
echo 方式一：使用 Gradle Wrapper（推荐）
echo   gradlew assembleDebug
echo.
echo 方式二：使用本地 Gradle（需提前安装）
echo   gradle assembleDebug
echo.
echo 方式三：使用 GitHub Actions 云端编译
echo   将代码推送到 GitHub，进入 Actions 页面触发
echo.

:menu
echo.
echo 请选择操作:
echo   [1] 编译 Debug APK
echo   [2] 编译 Release APK
echo   [3] 清理构建缓存
echo   [4] 打开项目目录
echo   [q] 退出
echo.
set /p action="请选择 (1/2/3/4/q): "

if "%action%"=="1" (
    echo.
    echo [⏳] 正在编译 Debug APK ...
    if exist gradlew.bat (
        call gradlew.bat assembleDebug
    ) else (
        echo [✗] 找不到 gradlew.bat，请先初始化 Gradle Wrapper
        echo 解决方案：gradle wrapper --gradle-version 8.5
    )
    echo.
    echo [✓] 编译完成！APK 位于:
    echo     app\build\outputs\apk\debug\
    pause
    goto menu
)

if "%action%"=="2" (
    echo.
    echo [⏳] 正在编译 Release APK ...
    if exist gradlew.bat (
        call gradlew.bat assembleRelease
    ) else (
        echo [✗] 找不到 gradlew.bat
    )
    echo.
    echo [✓] 编译完成！
    pause
    goto menu
)

if "%action%"=="3" (
    echo.
    echo [⏳] 正在清理 ...
    if exist gradlew.bat (
        call gradlew.bat clean
    )
    echo [✓] 清理完成
    pause
    goto menu
)

if "%action%"=="4" (
    start explorer .
    goto menu
)

if "%action%"=="q" (
    exit /b
)

goto menu
