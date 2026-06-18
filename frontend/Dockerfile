# ============================================================
# 前端 Dockerfile — 多阶段构建
# 阶段1: Node.js 构建
# 阶段2: Nginx 运行
# ============================================================

# ---------- 阶段1: 构建 ----------
FROM node:21-alpine AS builder

WORKDIR /app

# 复制依赖文件
COPY package.json package-lock.json* ./

# 安装依赖（使用国内镜像加速）
RUN npm ci --silent --registry=https://registry.npmmirror.com

# 复制源码
COPY . .

# 构建生产包
RUN npm run build

# ---------- 阶段2: 运行 ----------
FROM nginx:1.27-alpine

# 复制自定义 Nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 从构建阶段复制产物
COPY --from=builder /app/dist /usr/share/nginx/html

# 复制运行时配置（运维人员可修改此文件来更改后端地址）
COPY public/config.js /usr/share/nginx/html/config.js

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:80/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
