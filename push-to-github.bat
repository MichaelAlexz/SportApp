@echo off
chcp 65001 >nul
title 轻动APP - 上传到GitHub
color 0a

echo.
echo ============================================
echo     轻动运动APP - 上传到GitHub
echo ============================================
echo.

REM ---- 检查Git ----
echo [1/5] 检查Git...
git version >nul 2>&1
if %errorlevel% neq 0 (
    echo [失败] 没有安装Git！
    echo 请去 https://git-scm.com/download/win 下载安装
    pause
    exit /b
)
echo [OK] Git已就绪
echo.

REM ---- 输入Token ----
echo [2/5] 输入GitHub信息
echo ----------------------------------------
echo 你的Token: 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
echo ----------------------------------------
echo.

set TOKEN=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

set /p USERNAME="请输入你的GitHub用户名: "
if "%USERNAME%"=="" (
    echo [失败] 用户名不能为空
    pause
    exit /b
)

set /p REPO="仓库名 (直接回车用 SportApp): "
if "%REPO%"=="" set REPO=SportApp
echo.

REM ---- git init ----
echo [3/5] 初始化Git仓库...
if exist .git (
    echo Git仓库已存在，跳过
) else (
    git init >nul
    echo [OK] 初始化完成
)
echo.

REM ---- git add + commit ----
echo [4/5] 提交代码...
git add -A >nul
git commit -m "feat: initial commit" >nul
git branch -M main
echo [OK] 代码已提交
echo.

REM ---- push ----
echo [5/5] 推送到GitHub...
echo 正在推送，请稍等...
echo.

git remote remove origin >nul 2>&1
git remote add origin https://%TOKEN%@github.com/%USERNAME%/%REPO%.git

git push -u origin main 2>push_error.log

if %errorlevel% equ 0 (
    echo.
    echo ============================================
    echo     上传成功！
    echo ============================================
    echo.
    echo 仓库地址: https://github.com/%USERNAME%/%REPO%
    echo.
    echo 下一步：打开上面链接，点 Actions -%gt; Run workflow
    echo 等3-5分钟就能下载APK了！
) else (
    echo.
    echo [失败] 上传出错，请看下面提示：
    echo.
    type push_error.log
    echo.
    echo 常见原因：
    echo 1. GitHub用户名填错了
    echo 2. Token没有repo权限（去 https://github.com/settings/tokens 重新生成）
    echo 3. 网络不通
)

echo.
pause
