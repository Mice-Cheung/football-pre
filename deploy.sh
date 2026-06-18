#!/usr/bin/env bash
# ============================================================
# FootballPred 部署脚本 (Linux 服务器)
# 用途：运维人员一键部署（Docker 方式）
# 用法：
#   ./deploy.sh           # 部署或更新全栈应用
#   ./deploy.sh status    # 查看服务状态
#   ./deploy.sh logs      # 查看所有服务日志
#   ./deploy.sh restart   # 重启所有服务
#   ./deploy.sh stop      # 停止所有服务
#   ./deploy.sh health    # 健康检查
# ============================================================
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT_DIR"

ACTION="${1:-deploy}"

# ---------- 颜色输出 ----------
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

log_info()  { echo -e "${BLUE}[INFO]${NC}  $*"; }
log_ok()    { echo -e "${GREEN}[OK]${NC}    $*"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC}  $*"; }
log_error() { echo -e "${RED}[ERROR]${NC} $*"; }
log_title() { echo -e "${CYAN}$*${NC}"; }

# ---------- 环境检查 ----------
check_requirements() {
    log_info "===== 环境检查 ====="

    if ! command -v docker &>/dev/null; then
        log_error "未安装 Docker，请先安装 Docker Engine 20.10+"
        log_info "安装指南: https://docs.docker.com/engine/install/"
        exit 1
    fi
    log_ok "Docker $(docker --version)"

    if ! command -v docker-compose &>/dev/null && ! docker compose version &>/dev/null 2>&1; then
        log_error "未安装 docker-compose"
        log_info "安装指南: https://docs.docker.com/compose/install/"
        exit 1
    fi
    log_ok "Docker Compose 已就绪"

    echo ""
}

# ---------- 设置数据库密码 ----------
setup_env() {
    if [ ! -f ".env" ]; then
        log_info "===== 首次部署：设置数据库密码 ====="

        # 生成随机密码
        RANDOM_PW=$(openssl rand -base64 16 2>/dev/null || echo "ChangeMe$(date +%s)")

        read -r -p "请输入数据库密码（留空则自动生成随机密码）: " USER_PW
        DB_PASSWORD="${USER_PW:-$RANDOM_PW}"

        echo "DB_PASSWORD=${DB_PASSWORD}" > .env
        log_ok "密码已保存到 .env 文件"
        log_warn "请妥善保管 .env 文件，不要提交到版本控制！"
        echo ""
    else
        log_ok "已存在 .env 文件，使用已有配置"
    fi
}

# ---------- 部署 ----------
do_deploy() {
    log_title "============================================"
    log_title "  FootballPred 一键部署"
    log_title "============================================"
    echo ""

    check_requirements
    setup_env

    log_info "===== 构建并启动所有服务 ====="

    # 停止旧容器
    docker-compose down --remove-orphans 2>/dev/null || true

    # 构建镜像并启动
    docker-compose up -d --build

    echo ""
    log_info "===== 等待服务就绪 ====="

    # 等待后端健康检查通过
    MAX_WAIT=60
    WAITED=0
    while [ $WAITED -lt $MAX_WAIT ]; do
        if curl -s http://localhost:8080/api/health > /dev/null 2>&1; then
            log_ok "后端服务已就绪 (等待 ${WAITED}s)"
            break
        fi
        sleep 3
        WAITED=$((WAITED + 3))
        echo -n "."
    done
    echo ""

    if [ $WAITED -ge $MAX_WAIT ]; then
        log_warn "后端服务启动超时，请手动检查: docker-compose logs backend"
    fi

    echo ""
    log_title "============================================"
    log_title "  部署完成！"
    log_title "============================================"
    log_ok "前端页面 : http://localhost"
    log_ok "后端 API : http://localhost:8080/api"
    log_ok "健康检查 : http://localhost:8080/api/health"
    log_ok "Swagger  : http://localhost:8080/swagger-ui.html"
    log_info ""
    log_info "常用命令："
    log_info "  查看状态  : ./deploy.sh status"
    log_info "  查看日志  : ./deploy.sh logs"
    log_info "  重启服务  : ./deploy.sh restart"
    log_info "  停止服务  : ./deploy.sh stop"
    log_info "  健康检查  : ./deploy.sh health"
    log_title "============================================"
}

# ---------- 查看状态 ----------
do_status() {
    log_title "===== 服务状态 ====="
    echo ""
    docker-compose ps
    echo ""
    log_title "===== 资源使用 ====="
    docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}" \
        football-frontend football-backend football-postgres 2>/dev/null || true
}

# ---------- 查看日志 ----------
do_logs() {
    SERVICE="${2:-}"  # 可选：指定服务名
    if [ -n "$SERVICE" ]; then
        docker-compose logs -f --tail=100 "$SERVICE"
    else
        docker-compose logs -f --tail=100
    fi
}

# ---------- 重启 ----------
do_restart() {
    log_info "重启所有服务..."
    docker-compose restart
    log_ok "服务已重启"
}

# ---------- 停止 ----------
do_stop() {
    log_info "停止所有服务..."
    docker-compose down
    log_ok "所有服务已停止"
}

# ---------- 健康检查 ----------
do_health() {
    log_title "===== 健康检查 ====="
    echo ""

    # 检查后端
    log_info "检查后端服务..."
    if HEALTH=$(curl -s http://localhost:8080/api/health 2>/dev/null); then
        log_ok "后端服务: UP"
        echo "  $HEALTH" | python3 -m json.tool 2>/dev/null || echo "  $HEALTH"
    else
        log_error "后端服务: DOWN (无法连接 http://localhost:8080/api/health)"
    fi

    echo ""

    # 检查前端
    log_info "检查前端服务..."
    if curl -s -o /dev/null -w "%{http_code}" http://localhost/ | grep -q "200"; then
        log_ok "前端服务: UP (HTTP 200)"
    else
        log_error "前端服务: DOWN (无法连接 http://localhost)"
    fi

    echo ""

    # 检查数据库
    log_info "检查数据库连接..."
    if docker exec football-postgres pg_isready -U football -d football &>/dev/null; then
        log_ok "数据库: UP"
    else
        log_error "数据库: DOWN"
    fi

    echo ""
    log_title "===== 容器状态 ====="
    docker-compose ps
}

# ---------- 主流程 ----------
case "$ACTION" in
    deploy|"")
        do_deploy
        ;;
    status)
        do_status
        ;;
    logs)
        do_logs "$@"
        ;;
    restart)
        do_restart
        ;;
    stop)
        do_stop
        ;;
    health)
        do_health
        ;;
    *)
        echo "用法: $0 [deploy|status|logs|restart|stop|health]"
        echo ""
        echo "  deploy   部署或更新全栈应用 (默认)"
        echo "  status   查看所有服务状态"
        echo "  logs     查看服务日志 (可选: logs backend|frontend|postgres)"
        echo "  restart  重启所有服务"
        echo "  stop     停止所有服务"
        echo "  health   执行健康检查"
        exit 1
        ;;
esac
