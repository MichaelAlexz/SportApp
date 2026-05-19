# 轻动运动APP - 一键推送至 GitHub Actions
# 使用方法: powershell -ExecutionPolicy Bypass -File .\push-to-github.ps1

function Write-Success { Write-Host "[OK] $args" -ForegroundColor Green }
function Write-Info    { Write-Host "[..] $args" -ForegroundColor Cyan }
function Write-Error   { Write-Host "[XX] $args" -ForegroundColor Red }
function Write-Step    { Write-Host "`n[==>] $args" -ForegroundColor Yellow }

Clear-Host
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "   轻动运动APP - 云端编译" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host ""

# ---- 1. 检查 Git ----
try {
    $null = git version
} catch {
    Write-Error "未检测到 Git，请先安装 https://git-scm.com/download/win"
    pause; exit 1
}
Write-Success "Git 已就绪"

# ---- 2. 输入 Token ----
Write-Host ""
Write-Host "----------------------------------------" -ForegroundColor DarkGray
Write-Host "需要 GitHub Token" -ForegroundColor Cyan
Write-Host " 1. 打开 https://github.com/settings/tokens" -ForegroundColor Cyan
Write-Host " 2. Generate new token (classic)，勾选 repo+workflow" -ForegroundColor Cyan
Write-Host " 3. 复制那个 Token 粘贴到下面" -ForegroundColor Cyan
Write-Host "----------------------------------------" -ForegroundColor DarkGray
Write-Host ""

$token = Read-Host "请输入 Token (粘贴后回车)"
if ([string]::IsNullOrWhiteSpace($token)) {
    Write-Error "Token 不能为空"
    pause; exit 1
}

$repoName = Read-Host "`n仓库名 (直接回车默认: SportApp)"
if ([string]::IsNullOrWhiteSpace($repoName)) { $repoName = "SportApp" }

# ---- 3. 初始化 Git ----
Write-Step "1/4. 初始化"

$projectPath = Get-Location
if (Test-Path "$projectPath\.git") {
    Write-Info "Git 仓库已存在"
} else {
    git init 2>$null | Out-Null
    Write-Success "Git 仓库初始化成功"
}

$gitignore = "*.iml`n.gradle`n/local.properties`n/.idea`n.DS_Store`n/build`n/captures`.externalNativeBuild`.cxx`nlocal.properties"
[System.IO.File]::WriteAllText("$projectPath\.gitignore", $gitignore, [System.Text.Encoding]::UTF8)
Write-Success "已创建 .gitignore"

# ---- 4. 创建 GitHub 仓库 ----
Write-Step "2/4. 创建 GitHub 仓库"

$headers = @{
    Authorization = "Bearer $token"
    Accept = "application/vnd.github.v3+json"
}

try {
    $body = @{
        name = $repoName
        description = "轻动 - 运动记录APP"
        private = $false
        auto_init = $false
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "https://api.github.com/user/repos" -Method Post -Headers $headers -Body $body -ContentType "application/json"
    $owner = $response.owner.login
    Write-Success "仓库创建成功: https://github.com/$owner/$repoName"
} catch {
    try {
        $userResp = Invoke-RestMethod -Uri "https://api.github.com/user" -Headers $headers
        $owner = $userResp.login
        Write-Info "仓库可能已存在: https://github.com/$owner/$repoName"
    } catch {
        Write-Error "GitHub 认证失败，Token 无效或网络不通"
        pause; exit 1
    }
}

# ---- 5. 提交推送 ----
Write-Step "3/4. 提交代码"

git add -A 2>&1 | Out-Null
git commit -m "feat: initial commit" 2>&1 | Out-Null
git branch -M main

$remoteUrl = "https://x-access-token:$token@github.com/$owner/$repoName.git"
git remote remove origin 2>$null | Out-Null
git remote add origin $remoteUrl

Write-Info "正在推送代码到 GitHub..."
$pushResult = git push -u origin main 2>&1

if ($LASTEXITCODE -eq 0) {
    Write-Success "推送成功！"
} else {
    Write-Error "推送失败: $pushResult"
    Write-Info "请检查 Token 是否有 repo 权限"
    pause; exit 1
}

# ---- 6. 触发 Actions ----
Write-Step "4/4. 触发云端编译"

try {
    $dispatchBody = @{
        ref = "main"
        inputs = @{ build_type = "debug" }
    } | ConvertTo-Json

    $actionsUrl = "https://api.github.com/repos/$owner/$repoName/actions/workflows/build-apk.yml/dispatches"
    Invoke-RestMethod -Uri $actionsUrl -Method Post -Headers $headers -Body $dispatchBody -ContentType "application/json" -ErrorAction Stop
    Write-Success "云端编译已触发！"
} catch {
    Write-Info "请手动触发编译:"
    Write-Info "打开 https://github.com/$owner/$repoName/actions"
    Write-Info "点击 [Build APK] -> [Run workflow]"
}

# ---- 完成 ----
Write-Host ""
Write-Host "==========================================" -ForegroundColor Green
Write-Host "  全部完成！" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Green
Write-Host ""
Write-Host "仓库: https://github.com/$owner/$repoName" -ForegroundColor Cyan
Write-Host "Actions: https://github.com/$owner/$repoName/actions" -ForegroundColor Cyan
Write-Host ""
Write-Host "等 3-5 分钟后在 Actions 页面下载 APK" -ForegroundColor Yellow
Write-Host ""

pause
