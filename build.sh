#!/usr/bin/env bash
# ============================================================
# FootballPred 一键构建脚本 (Linux/macOS)
# 用途：研发人员一键完成前后端打包
# 用法：
#   ./build.sh          # 默认：同时构建前后端
#   ./build.sh backend  # 只构建后端
#   ./build.sh frontend # 只构建前端
#   ./build.sh clean    # 清理所有构建产物
# ============================================================
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
BUILD_TARGET="${1:-all}"

# ---------- 颜色输出 ----------
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

log_info()  { echo -e "${BLUE}[INFO]${NC}  $*"; }
log_ok()    { echo -e "${GREEN}[OK]${NC}    $*"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC}  $*"; }
log_error() { echo -e "${RED}[ERROR]${NC} $*"; }

# ---------- 环境检查 ----------
check_env() {
    log_info "===== 环境检查 ====="

    # 检查 Java 21
    if command -v java &>/dev/null; then
        JAVA_VER=$(java -version 2>&1 | head -1 | grep -oP '\d+\.\d+\.\d+' | cut -d. -f1)
        if [ "${JAVA_VER:-0}" -ge 21 ]; then
            log_ok "Java $(java -version 2>&1 | head -1)"
        else
            log_warn "Java 版本过低 (需要 >=21)，当前: $(java -version 2>&1 | head -1)"
        fi
    else
        log_error "未找到 Java，请安装 JDK 21+"
        exit 1
    fi

    # 检查 Maven
    if command -v mvn &>/dev/null; then
        log_ok "Maven $(mvn -version 2>&1 | head -1)"
    else
        log_error "未找到 Maven，请安装 Maven 3.9+"
        exit 1
    fi

    # 检查 Node.js
    if command -v node &>/dev/null; then
        NODE_VER=$(node -v | grep -oP '\d+' | head -1)
        if [ "${NODE_VER:-0}" -ge 20 ]; then
            log_ok "Node.js $(node -v)"
        else
            log_warn "Node.js 版本过低 (需要 >=20)，当前: $(node -v)"
        fi
    else
        log_error "未找到 Node.js，请安装 Node.js 20+"
        exit 1
    fi

    # 检查 npm
    if command -v npm &>/dev/null; then
        log_ok "npm $(npm -v)"
    else
        log_error "未找到 npm"
        exit 1
    fi

    echo ""
}

# ---------- 构建后端 ----------
build_backend() {
    log_info "===== 构建后端 (Spring Boot) ====="
    cd "$ROOT_DIR/backend"

    # 执行 Maven 打包
    mvn clean package -DskipTests -q

    if [ -f "target/football-backend-1.0.0.jar" ]; then
        JAR_SIZE=$(du -h target/football-backend-1.0.0.jar | cut -f1)
        log_ok "后端打包成功: target/football-backend-1.0.0.jar (${JAR_SIZE})"
    else
        log_error "后端打包失败，JAR 文件未生成"
        exit 1
    fi
}

# ---------- 构建前端 ----------
build_frontend() {
    log_info "===== 构建前端 (Vue 3 + Vite) ====="
    cd "$ROOT_DIR/frontend"

    # 安装依赖（如有变更）
    if [ ! -d "node_modules" ] || [ "package.json" -nt "node_modules/.package-lock.json" ] 2>/dev/null; then
        log_info "安装前端依赖..."
        npm install --silent
    fi

    # 执行构建
    npm run build

    if [ -d "dist" ] && [ -f "dist/index.html" ]; then
        log_ok "前端打包成功: dist/"
    else
        log_error "前端打包失败，dist/ 目录未生成"
        exit 1
    fi
}

# ---------- 清理构建产物 ----------
clean_all() {
    log_info "===== 清理构建产物 ====="

    cd "$ROOT_DIR/backend"
    mvn clean -q 2>/dev/null || true
    rm -rf target
    log_ok "后端 target/ 已清理"

    cd "$ROOT_DIR/frontend"
    rm -rf dist node_modules/.vite 2>/dev/null || true
    log_ok "前端 dist/ 已清理"

    log_ok "清理完成"
}

# ---------- 生成构建摘要 ----------
print_summary() {
    echo ""
    echo "============================================"
    echo "  构建完成！产物位置："
    echo "============================================"
    echo "  后端 JAR : backend/target/football-backend-1.0.0.jar"
    echo "  前端静态 : frontend/dist/"
    echo ""
    echo "  >>> 下一步：将产物交给运维人员部署 <<<"
    echo "  或者使用 Docker 一键部署：docker-compose up -d"
    echo "============================================"
}

# ---------- 主流程 ----------
echo ""
echo "============================================"
echo "  FootballPred 项目构建脚本"
echo "============================================"
echo ""

case "$BUILD_TARGET" in
    backend)
        check_env
        build_backend
        ;;
    frontend)
        check_env
        build_frontend
        ;;
    clean)
        clean_all
        exit 0
        ;;
    all|"")
        check_env
        build_backend
        build_frontend
        print_summary
        ;;
    *)
        echo "用法: $0 [backend|frontend|clean]"
        echo "  backend  只构建后端 JAR 包"
        echo "  frontend 只构建前端静态文件"
        echo "  clean    清理所有构建产物"
        echo "  (无参数) 同时构建前后端"
        exit 1
        ;;
esac
