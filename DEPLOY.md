# FootballPred 部署文档

> 足球赛事数据平台 — 前后端分离架构  
> 适用环境：Linux（推荐 CentOS 7+ / Ubuntu 20.04+）、Windows Server、macOS

---

## 目录

- [1. 快速部署（Docker 方式，推荐）](#1-快速部署docker-方式推荐)
- [2. 研发打包指引](#2-研发打包指引)
- [3. 传统手动部署](#3-传统手动部署)
- [4. 运维排查指南](#4-运维排查指南)
- [附录 A：配置文件参考](#附录-a配置文件参考)
- [附录 B：API 接口清单](#附录-bapi-接口清单)

---

## 1. 快速部署（Docker 方式，推荐）

> **运维人员首选**：一键部署，无需手动安装 JDK/Maven/Node.js/Nginx。

### 1.1 环境要求

| 组件 | 版本要求 |
|------|----------|
| Docker Engine | 20.10+ |
| Docker Compose | v2.0+ |

```bash
# 验证环境
docker --version
docker compose version
```

### 1.2 一键部署

```bash
# 1. 克隆项目（或从研发获取项目目录）
git clone <your-repo-url>
cd football-pre

# 2. 执行部署脚本
chmod +x deploy.sh
./deploy.sh
```

首次部署会提示设置数据库密码，之后自动完成：
- 构建前端镜像（Node.js 编译 → Nginx 运行）
- 构建后端镜像（Maven 编译 → JRE 运行）
- 启动 PostgreSQL 数据库
- 启动所有服务并验证健康状态

### 1.3 常用运维命令

```bash
./deploy.sh status     # 查看所有服务运行状态和资源使用
./deploy.sh logs       # 查看所有服务日志（实时）
./deploy.sh logs backend  # 只看后端日志
./deploy.sh health    # 执行完整健康检查
./deploy.sh restart   # 重启所有服务
./deploy.sh stop      # 停止所有服务
```

### 1.4 服务访问

| 服务 | 地址 |
|------|------|
| 前端页面 | http://localhost |
| 后端 API | http://localhost:8080/api |
| 健康检查 | http://localhost:8080/api/health |
| Swagger 文档 | http://localhost:8080/swagger-ui.html |

### 1.5 更新部署

当研发交付新版本后：

```bash
# 拉取最新代码后重新部署
git pull
./deploy.sh
```

脚本会自动重建镜像并重启服务。

### 1.6 自定义配置

**修改数据库密码**（首次部署后）：
```bash
# 编辑 .env 文件
vi .env
# 修改 DB_PASSWORD 的值，然后重启
./deploy.sh restart
```

**修改后端 JVM 内存**：
编辑 `docker-compose.yml`，修改 `JAVA_OPTS` 环境变量：
```yaml
JAVA_OPTS: "-Xms512m -Xmx1024m"
```

---

## 2. 研发打包指引

> **研发人员**：在本地完成开发后，使用以下方式打包交付给运维。

### 2.1 方式一：一键构建脚本（推荐）

```bash
# Linux/macOS
chmod +x build.sh
./build.sh           # 同时构建前后端

# Windows PowerShell
.\build.ps1          # 同时构建前后端
```

脚本会自动：
1. 检查 JDK 21、Maven、Node.js 环境
2. 编译后端 JAR 包 → `backend/target/football-backend-1.0.0.jar`
3. 编译前端静态文件 → `frontend/dist/`
4. 输出构建摘要

**可选参数：**
```bash
./build.sh backend   # 只构建后端
./build.sh frontend  # 只构建前端
./build.sh clean     # 清理所有构建产物
```

### 2.2 方式二：手动分步构建

#### 后端

```bash
cd backend
mvn clean package -DskipTests
# 产物: target/football-backend-1.0.0.jar
```

#### 前端

```bash
cd frontend
npm install         # 仅首次或依赖变更后执行
npm run build
# 产物: dist/
```

### 2.3 构建产物交付清单

将以下内容交付给运维人员：

```
交付包/
├── docker-compose.yml        # Docker 编排文件
├── deploy.sh                  # 部署脚本
├── .env.example               # 环境变量模板（可选）
├── backend/
│   ├── Dockerfile             # 后端镜像构建文件
│   └── (源码目录，Docker 构建时使用)
├── frontend/
│   ├── Dockerfile             # 前端镜像构建文件
│   ├── nginx.conf             # Nginx 配置
│   └── (源码目录，Docker 构建时使用)
└── README.md                  # 项目说明
```

> **提示**：如果使用 Docker 方式部署，运维人员不需要手动安装 JDK/Maven/Node.js，Docker 构建阶段会自动处理。

---

## 3. 传统手动部署

> 如果不想使用 Docker，按以下步骤手动部署。

### 3.1 环境要求

| 组件 | 版本要求 | 用途 |
|------|----------|------|
| JDK | **21** | 后端运行环境 |
| Maven | 3.9+ | 后端编译构建 |
| Node.js | 20 LTS+ | 前端编译构建 |
| npm | 10+ | 前端依赖管理 |
| PostgreSQL | 16+ | 生产数据库 |
| Nginx | 1.24+ | 前端静态资源 + 反向代理 |

### 3.2 后端部署

#### 环境准备

**安装 JDK 21：**
```bash
# Ubuntu/Debian
sudo apt update && sudo apt install openjdk-21-jdk
java -version
```

**安装 Maven：**
```bash
sudo apt install maven
mvn -version
```

**安装 PostgreSQL：**
```bash
sudo apt install postgresql postgresql-contrib
sudo systemctl enable postgresql
sudo systemctl start postgresql

# 创建数据库
sudo -u postgres psql
CREATE DATABASE football;
ALTER USER postgres PASSWORD 'your_secure_password';
\q
```

#### 编译构建

```bash
cd backend
mvn clean package -DskipTests
# 产物: target/football-backend-1.0.0.jar
```

#### 配置与启动

```bash
# 设置环境变量
export DB_PASSWORD="your_secure_password"

# 前台运行（调试）
java -jar target/football-backend-1.0.0.jar -Dspring.profiles.active=prod

# systemd 服务（生产推荐）
sudo tee /etc/systemd/system/football-backend.service << 'EOF'
[Unit]
Description=FootballPred Backend Service
After=network.target postgresql.service
Wants=postgresql.service

[Service]
Type=simple
User=appuser
WorkingDirectory=/opt/football/backend
Environment="DB_PASSWORD=your_secure_password"
ExecStart=/usr/bin/java -jar /opt/football/backend/football-backend-1.0.0.jar \
  -Dspring.profiles.active=prod \
  -Xms512m -Xmx1024m
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable --now football-backend
```

#### 验证

```bash
# 健康检查端点
curl http://localhost:8080/api/health

# 业务 API
curl http://localhost:8080/api/matches/leagues
```

### 3.3 前端部署

#### 构建

```bash
cd frontend
npm install
npm run build
# 产物: dist/
```

#### Nginx 部署

```bash
sudo apt install nginx
sudo mkdir -p /var/www/football
sudo cp -r dist/* /var/www/football/

sudo tee /etc/nginx/conf.d/football.conf << 'EOF'
server {
    listen 80;
    server_name _;

    root /var/www/football;
    index index.html;

    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml text/javascript;
    gzip_min_length 1k;
    gzip_comp_level 6;

    location /assets/ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location / {
        try_files $uri $uri/ /index.html;
    }

    location ~ /\. {
        deny all;
    }
}
EOF

sudo nginx -t && sudo systemctl reload nginx
```

---

## 4. 运维排查指南

### 4.1 健康检查速查表

```bash
# ===== 快速健康检查（Docker 方式） =====
./deploy.sh health

# ===== 快速健康检查（手动方式） =====
# 后端
curl http://localhost:8080/api/health
# 前端
curl -o /dev/null -s -w "%{http_code}" http://localhost/
# 数据库
psql -h localhost -U football -d football -c "SELECT 1"
```

### 4.2 日志查看

```bash
# ===== Docker 方式 =====
./deploy.sh logs              # 所有服务日志
./deploy.sh logs backend      # 后端日志
./deploy.sh logs frontend     # 前端日志
./deploy.sh logs postgres     # 数据库日志

# ===== 手动方式 =====
# 后端日志（按天滚动）
tail -f backend/logs/football.log
tail -f backend/logs/error.log   # 仅错误日志

# 后端 systemd 日志
sudo journalctl -u football-backend -f --since "10 min ago"

# Nginx 日志
tail -f /var/log/nginx/access.log
tail -f /var/log/nginx/error.log
```

### 4.3 常见问题排查

#### Q1：后端启动报错 "Failed to configure a DataSource"

**Docker 方式：**
```bash
# 检查数据库容器状态
docker-compose ps postgres
# 查看数据库日志
./deploy.sh logs postgres
# 检查数据库连接
docker exec football-postgres pg_isready -U football -d football
```

**手动方式：**
```bash
systemctl status postgresql
psql -h localhost -U postgres -d football -W
```

#### Q2：前端页面空白，控制台报 404

**原因**：Vue Router history 模式需要服务端回退配置。

**Docker 方式**：Nginx 配置已包含 `try_files` 规则，一般不会出现。

**手动方式**：确认 Nginx 配置包含 `try_files $uri $uri/ /index.html;`。

#### Q3：前端 API 请求失败

```bash
# 检查后端是否运行
docker-compose ps backend           # Docker
systemctl status football-backend   # 手动

# 测试后端接口
curl http://localhost:8080/api/health

# Docker 方式：检查网络连通性
docker exec football-frontend wget -q -O- http://backend:8080/api/health
```

#### Q4：跨域错误（CORS）

后端 `WebConfig.java` 默认允许所有来源。如果修改过，检查是否允许前端域名。

#### Q5：内存不足 OOM

**Docker 方式**：修改 `docker-compose.yml` 中 `JAVA_OPTS`：
```yaml
JAVA_OPTS: "-Xms512m -Xmx1024m"  # 根据服务器内存调整
```

**手动方式**：修改 systemd 服务文件中的 `-Xms` 和 `-Xmx` 参数。

#### Q6：数据库备份与恢复

```bash
# 备份（Docker 方式）
docker exec football-postgres pg_dump -U football -d football -F c \
  -f /tmp/football_$(date +%Y%m%d).dump
docker cp football-postgres:/tmp/football_$(date +%Y%m%d).dump .

# 备份（手动方式）
pg_dump -U postgres -d football -F c -f football_$(date +%Y%m%d).dump

# 恢复
docker exec -i football-postgres pg_restore -U football -d football < football_20260618.dump
```

#### Q7：磁盘空间不足

```bash
# 清理 Docker 无用资源
docker system prune -a --volumes

# 清理旧日志（后端日志保留 30 天，ERROR 日志保留 60 天）
# 如果仍然不足，检查 logback-spring.xml 中的 maxHistory 配置
```

### 4.4 监控检查点

建议运维监控以下端点：

| 检查项 | 端点/命令 | 预期 |
|--------|-----------|------|
| 后端存活 | `GET /api/health` | HTTP 200, status=UP |
| 后端就绪 | `GET /api/ready` | HTTP 200 |
| 业务接口 | `GET /api/matches/leagues` | HTTP 200, JSON |
| 前端可访问 | `GET /` | HTTP 200 |
| 数据库连通 | `pg_isready` | accepting connections |
| 容器运行 | `docker-compose ps` | 全部 Up |

### 4.5 应急重启流程

```bash
# Docker 方式
./deploy.sh stop
./deploy.sh           # 重新部署

# 手动方式
sudo systemctl restart football-backend
sudo systemctl reload nginx
```

---

## 附录 A：配置文件参考

### application.yml（通用配置）

```yaml
server:
  port: 8080

spring:
  profiles:
    active: dev

  jackson:
    time-zone: Asia/Shanghai
    date-format: yyyy-MM-dd HH:mm:ss
    default-property-inclusion: non_null

  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect

logging:
  level:
    com.football: DEBUG
    org.hibernate.SQL: DEBUG

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    operationsSorter: method
```

### application-prod.yml（生产覆盖配置）

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/football
    driver-class-name: org.postgresql.Driver
    username: postgres
    password: ${DB_PASSWORD:postgres}

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false

logging:
  level:
    com.football: INFO

football:
  datasource:
    sync-enabled: true
```

### logback-spring.xml（日志配置）

- **日志文件位置**: `logs/football.log`（所有级别）、`logs/error.log`（仅 ERROR）
- **滚动策略**: 按天滚动，单文件最大 50MB，自动压缩为 `.gz`
- **保留时间**: 普通日志 30 天，错误日志 60 天
- **Docker 环境**: 日志通过数据卷 `backend_logs` 持久化

### 前端 API 配置

前端支持**构建时注入 + 运行时覆盖**双重配置：

**运行时覆盖（推荐，无需重新构建）**：
修改 `frontend/public/config.js`（构建后位于 `dist/config.js`）：
```javascript
window.__APP_CONFIG__ = {
  API_BASE_URL: 'https://api.example.com/api',
}
```

**构建时注入**：
```bash
VITE_API_BASE_URL=https://api.example.com/api npm run build
```

---

## 附录 B：API 接口清单

| 接口 | 方法 | 参数 | 说明 |
|------|------|------|------|
| `/api/health` | GET | - | **健康检查（新增）** |
| `/api/ready` | GET | - | **就绪检查（新增）** |
| `/api/matches` | GET | `league?` `status?` `page?` `size?` | 比赛列表（分页+筛选） |
| `/api/matches/{id}` | GET | - | 比赛基本信息 |
| `/api/matches/{id}/detail` | GET | - | 比赛详情（含阵容） |
| `/api/matches/leagues` | GET | - | 可用联赛列表 |
| `/api/teams/{id}` | GET | - | 球队详情 |
| `/api/teams/lineups/match/{matchId}` | GET | - | 指定比赛双方阵容 |
| `/api/odds/match/{matchId}` | GET | - | 指定比赛全部赔率 |
| `/api/odds/match/{matchId}/comparison` | GET | - | 中外赔率对比（核心接口） |
| `/api/odds/kelly/match/{matchId}` | GET | - | 凯利指数 |
| `/api/tactics/match/{matchId}` | GET | - | 技战术数据 |

完整 API 文档：启动后端后访问 http://localhost:8080/swagger-ui.html

---

> **文档版本**: 2.0  
> **最后更新**: 2026-06-18  
> **新增内容**: Docker 部署、构建脚本、健康检查端点、日志配置、排查速查表
