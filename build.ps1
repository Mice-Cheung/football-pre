<#
.SYNOPSIS
    FootballPred 一键构建脚本 (Windows PowerShell)
.DESCRIPTION
    研发人员一键完成前后端打包
.PARAMETER Target
    构建目标: all(默认), backend, frontend, clean
.EXAMPLE
    .\build.ps1              # 同时构建前后端
    .\build.ps1 backend      # 只构建后端
    .\build.ps1 frontend     # 只构建前端
    .\build.ps1 clean        # 清理所有构建产物
#>
param(
    [ValidateSet("all", "backend", "frontend", "clean")]
    [string]$Target = "all"
)

$ErrorActionPreference = "Stop"
$RootDir = Split-Path -Parent $MyInvocation.MyCommand.Path

function Write-Step { Write-Host "[INFO]  $args" -ForegroundColor Blue }
function Write-Ok    { Write-Host "[OK]    $args" -ForegroundColor Green }
function Write-Warn  { Write-Host "[WARN]  $args" -ForegroundColor Yellow }
function Write-Err   { Write-Host "[ERROR] $args" -ForegroundColor Red }

# ---------- 环境检查 ----------
function Check-Env {
    Write-Step "===== 环境检查 ====="

    # 检查 Java 21
    $javaCmd = Get-Command java -ErrorAction SilentlyContinue
    if ($javaCmd) {
        $javaVerOutput = java -version 2>&1 | Select-Object -First 1
        if ($javaVerOutput -match '(\d+)') {
            $ver = [int]$Matches[1]
            if ($ver -ge 21) {
                Write-Ok "Java $javaVerOutput"
            } else {
                Write-Warn "Java 版本过低 (需要 >=21)，当前: $javaVerOutput"
            }
        }
    } else {
        Write-Err "未找到 Java，请安装 JDK 21+"
        exit 1
    }

    # 检查 Maven
    $mvnCmd = Get-Command mvn -ErrorAction SilentlyContinue
    if ($mvnCmd) {
        $mvnVer = mvn -version 2>&1 | Select-Object -First 1
        Write-Ok "Maven $mvnVer"
    } else {
        Write-Err "未找到 Maven，请安装 Maven 3.9+"
        exit 1
    }

    # 检查 Node.js
    $nodeCmd = Get-Command node -ErrorAction SilentlyContinue
    if ($nodeCmd) {
        $nodeVer = node -v
        Write-Ok "Node.js $nodeVer"
    } else {
        Write-Err "未找到 Node.js，请安装 Node.js 20+"
        exit 1
    }

    # 检查 npm
    $npmCmd = Get-Command npm -ErrorAction SilentlyContinue
    if ($npmCmd) {
        $npmVer = npm -v
        Write-Ok "npm $npmVer"
    } else {
        Write-Err "未找到 npm"
        exit 1
    }

    Write-Host ""
}

# ---------- 构建后端 ----------
function Build-Backend {
    Write-Step "===== 构建后端 (Spring Boot) ====="
    Set-Location "$RootDir\backend"

    mvn clean package -DskipTests -q

    $jarPath = "target\football-backend-1.0.0.jar"
    if (Test-Path $jarPath) {
        $size = (Get-Item $jarPath).Length
        $sizeMB = [math]::Round($size / 1MB, 2)
        Write-Ok "后端打包成功: $jarPath ($sizeMB MB)"
    } else {
        Write-Err "后端打包失败，JAR 文件未生成"
        exit 1
    }
}

# ---------- 构建前端 ----------
function Build-Frontend {
    Write-Step "===== 构建前端 (Vue 3 + Vite) ====="
    Set-Location "$RootDir\frontend"

    # 安装依赖
    if (-not (Test-Path "node_modules")) {
        Write-Step "安装前端依赖..."
        npm install --silent
    }

    npm run build

    if ((Test-Path "dist") -and (Test-Path "dist\index.html")) {
        Write-Ok "前端打包成功: dist\"
    } else {
        Write-Err "前端打包失败，dist\ 目录未生成"
        exit 1
    }
}

# ---------- 清理构建产物 ----------
function Clear-All {
    Write-Step "===== 清理构建产物 ====="

    Set-Location "$RootDir\backend"
    mvn clean -q 2>$null
    if (Test-Path "target") { Remove-Item -Recurse -Force "target" }
    Write-Ok "后端 target\ 已清理"

    Set-Location "$RootDir\frontend"
    if (Test-Path "dist") { Remove-Item -Recurse -Force "dist" }
    Write-Ok "前端 dist\ 已清理"

    Write-Ok "清理完成"
}

# ---------- 打印摘要 ----------
function Print-Summary {
    Write-Host ""
    Write-Host "============================================"
    Write-Host "  构建完成！产物位置：" -ForegroundColor Green
    Write-Host "============================================"
    Write-Host "  后端 JAR : backend\target\football-backend-1.0.0.jar"
    Write-Host "  前端静态 : frontend\dist\"
    Write-Host ""
    Write-Host "  >>> 下一步：将产物交给运维人员部署 <<<" -ForegroundColor Yellow
    Write-Host "  或者使用 Docker 一键部署：docker-compose up -d"
    Write-Host "============================================"
}

# ---------- 主流程 ----------
Write-Host ""
Write-Host "============================================" -ForegroundColor Green
Write-Host "  FootballPred 项目构建脚本" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
Write-Host ""

switch ($Target) {
    "backend"  { Check-Env; Build-Backend }
    "frontend" { Check-Env; Build-Frontend }
    "clean"    { Clear-All }
    "all"      { Check-Env; Build-Backend; Build-Frontend; Print-Summary }
}
