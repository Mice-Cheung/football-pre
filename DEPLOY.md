# FootballPred 部署文档

> 足球赛事数据平台 — 前后端分离架构  
> 适用环境：Linux（推荐 CentOS 7+ / Ubuntu 20.04+）、Windows Server、macOS

---

## 目录

- [1. 环境要求](#1-环境要求)
- [2. 后端部署](#2-后端部署)
  - [2.1 环境准备](#21-环境准备)
  - [2.2 编译构建](#22-编译构建)
  - [2.3 生产环境配置](#23-生产环境配置)
  - [2.4 启动运行](#24-启动运行)
  - [2.5 验证后端](#25-验证后端)
- [3. 前端独立部署配置](#3-前端独立部署配置)
  - [3.1 API 地址配置方式](#31-api-地址配置方式)
  - [3.2 后端 CORS 配置](#32-后端-cors-配置)
- [4. 前端部署](#4-前端部署)
  - [4.1 环境准备](#41-环境准备)
  - [4.2 构建生产包](#42-构建生产包)
  - [4.3 Nginx 部署](#43-nginx-部署)
  - [4.4 验证前端](#44-验证前端)
- [5. 完整部署检查清单](#5-完整部署检查清单)
- [6. 常见问题排查](#6-常见问题排查)
- [7. 运维建议](#7-运维建议)
- [附录 A：配置文件参考](#附录-a配置文件参考)
- [附录 B：API 接口清单](#附录-bapi-接口清单)

---

## 1. 环境要求

| 组件 | 版本要求 | 用途 |
|------|----------|------|
| JDK | **21** | 后端运行环境 |
| Maven | 3.9+ | 后端编译构建 |
| Node.js | 20 LTS+ | 前端编译构建 |
| npm | 10+ | 前端依赖管理 |
| PostgreSQL | 16+ | 生产数据库 |
| Nginx | 1.24+ | 前端静态资源 + 反向代理 |

**请务必确认 JDK 版本为 21**（`java -version`），项目使用 Spring Boot 3.3，最低要求 JDK 17，但 `pom.xml` 中指定的 `<java.version>` 为 21。

---

## 2. 后端部署

### 2.1 环境准备

#### 安装 JDK 21

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install openjdk-21-jdk
java -version   # 确认输出含 "21"
```

**Linux (CentOS/RHEL):**
```bash
sudo yum install java-21-openjdk-devel
java -version
```

**Windows:**
从 [Adoptium](https://adoptium.net/) 下载 OpenJDK 21 MSI 安装包，安装后设置环境变量 `JAVA_HOME`。

#### 安装 Maven

**Linux:**
```bash
sudo apt install maven     # Ubuntu/Debian
sudo yum install maven     # CentOS/RHEL
mvn -version
```

**Windows:**
从 [maven.apache.org](https://maven.apache.org/download.cgi) 下载解压，将 `bin/` 加入 `PATH`。

#### 安装和配置 PostgreSQL

**Linux (Ubuntu/Debian):**
```bash
sudo apt install postgresql postgresql-contrib
sudo systemctl enable postgresql
sudo systemctl start postgresql
```

**Linux (CentOS/RHEL):**
```bash
sudo yum install postgresql-server postgresql-contrib
sudo postgresql-setup initdb
sudo systemctl enable postgresql
sudo systemctl start postgresql
```

**创建数据库和用户:**
```bash
sudo -u postgres psql
```

```sql
-- 创建数据库
CREATE DATABASE football;

-- 设置密码（替换 your_secure_password）
ALTER USER postgres PASSWORD 'your_secure_password';

-- 验证连接
\c football
\q
```

> ⚠️ **安全提示**：生产环境请勿使用默认密码，务必修改为强密码。

#### 验证数据库连接

```bash
psql -h localhost -U postgres -d football -W
# 输入密码后应成功进入 psql 命令行
```

---

### 2.2 编译构建

```bash
# 进入后端项目目录
cd backend

# 清理并编译打包（跳过测试，首次部署推荐）
mvn clean package -DskipTests

# 打包完成后，JAR 文件位于：
ls -lh target/football-backend-1.0.0.jar
```

> **说明**：`-DskipTests` 跳过单元测试。如果数据库未就绪，测试会因无法连接而失败。数据库准备好后可去掉此参数。

---

### 2.3 生产环境配置

**核心区别**：开发环境使用 H2 内存数据库并自动生成 Mock 数据，生产环境使用 PostgreSQL 且不会自动写入 Mock 数据。

#### 配置方式一：环境变量（推荐）

```bash
# 数据库密码（必设）
export DB_PASSWORD="your_secure_password"

# 如需修改数据库地址/端口（可选，默认 localhost:5432）
export DB_URL="jdbc:postgresql://your-host:5432/football"
export DB_USERNAME="postgres"
```

#### 配置方式二：修改配置文件

编辑 `backend/src/main/resources/application-prod.yml`：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/football
    driver-class-name: org.postgresql.Driver
    username: postgres
    password: your_secure_password       # 直接写入（不推荐，建议用环境变量）

  jpa:
    hibernate:
      ddl-auto: update                   # 首次启动自动建表，后续更新表结构
    show-sql: false
```

> **`ddl-auto` 说明：**
> - `update`：自动创建/更新表结构，不删除已有数据（**生产推荐**）
> - `create-drop`：每次启动重建表，关闭时删除（*仅在 dev 环境使用*）
> - `validate`：仅校验实体与表结构是否匹配，不自动修改

#### 配置文件优先级（由低到高）

1. `application.yml`（通用配置）
2. `application-prod.yml`（生产环境覆盖配置）
3. 环境变量（最高优先级，覆盖 yml 配置）

profile 由启动参数 `-Dspring.profiles.active=prod` 指定。

---

### 2.4 启动运行

#### 前台运行（调试用）

```bash
java -jar target/football-backend-1.0.0.jar \
  -Dspring.profiles.active=prod \
  -DDB_PASSWORD=your_secure_password
```

按 `Ctrl+C` 停止。

#### 后台运行（systemd 服务，推荐生产方式）

**创建 systemd 服务文件：**

```bash
sudo tee /etc/systemd/system/football-backend.service << 'EOF'
[Unit]
Description=FootballPred Backend Service
After=network.target postgresql.service
Wants=postgresql.service

[Service]
Type=simple
User=appuser                           # 替换为实际运行用户
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
```

**启动服务：**

```bash
# 1. 将 JAR 包复制到部署目录
sudo mkdir -p /opt/football/backend
sudo cp target/football-backend-1.0.0.jar /opt/football/backend/

# 2. 重新加载 systemd 并启动
sudo systemctl daemon-reload
sudo systemctl enable football-backend
sudo systemctl start football-backend

# 3. 查看状态和日志
sudo systemctl status football-backend
sudo journalctl -u football-backend -f    # 实时日志
```

**JVM 参数说明：**

| 参数 | 说明 |
|------|------|
| `-Xms512m` | 初始堆内存 512MB |
| `-Xmx1024m` | 最大堆内存 1GB（根据服务器内存调整） |

#### 停止/重启

```bash
sudo systemctl stop football-backend
sudo systemctl restart football-backend
```

---

### 2.5 验证后端

```bash
# 1. 健康检查（应返回数据）
curl http://localhost:8080/api/matches/leagues

# 2. Swagger 文档页
# 浏览器访问：http://your-server-ip:8080/swagger-ui.html

# 3. API 文档 JSON
curl http://localhost:8080/api-docs
```

---

## 3. 前端独立部署配置

> 前端支持**构建时注入** + **运行时覆盖**双重 API 地址配置，可独立部署到任意静态服务器。

### 3.1 API 地址配置方式

**优先级：运行时配置 > 构建时环境变量 > 硬编码兜底值**

#### 方式一：运行时覆盖（推荐，部署后无需重新构建）

修改 `frontend/public/config.js`（构建后位于 `dist/config.js`）：

```javascript
window.__APP_CONFIG__ = {
  API_BASE_URL: 'https://api.example.com/api',  // 改为实际后端地址
}
```

修改后刷新浏览器即可生效。

#### 方式二：构建时注入（适合 CI/CD 流水线）

通过环境变量 `VITE_API_BASE_URL` 注入：

```bash
# 方式 A：创建 .env.production 文件
echo "VITE_API_BASE_URL=https://api.example.com/api" > frontend/.env.production

# 方式 B：构建时传入
cd frontend
VITE_API_BASE_URL=https://api.example.com/api npm run build
```

#### 默认配置说明

| 文件 | 用途 | 默认值 |
|------|------|--------|
| `.env` | 开发环境（`npm run dev`） | `http://localhost:8080/api` |
| `.env.production` | 生产构建（`npm run build`） | `/api`（相对路径） |

### 3.2 后端 CORS 配置

前端独立部署在不同域名时，后端需允许跨域。当前 `WebConfig.java` 已配置允许所有来源（`*`），**无需修改**。

如需限制为指定域名，修改 `backend/src/main/java/com/football/config/WebConfig.java`：

```java
// 将 setAllowedOriginPatterns(List.of("*")) 改为：
config.setAllowedOriginPatterns(List.of("https://your-frontend-domain.com"));
```

---

## 4. 前端部署

### 4.1 环境准备

#### 安装 Node.js 20+

**Linux (使用 NodeSource):**
```bash
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs
node -v     # 应显示 v20.x.x
npm -v      # 应显示 10.x.x
```

**Windows:**
从 [nodejs.org](https://nodejs.org/) 下载 LTS 版本安装。

#### 安装依赖并构建

```bash
# 进入前端项目目录
cd frontend

# 安装依赖（仅首次或 package.json 变更后执行）
npm install

# 构建生产包
npm run build

# 构建产物位于 dist/ 目录
ls -lh dist/
```

构建产物 `dist/` 目录结构：
```
dist/
├── index.html
├── assets/
│   ├── index-xxxxx.js      # 业务代码（压缩后）
│   ├── index-xxxxx.css     # 样式文件
│   └── ...
└── vite.svg
```

---

### 4.2 部署方式

#### 方式一：Nginx 静态部署（推荐）

**安装 Nginx：**

```bash
# Ubuntu/Debian
sudo apt install nginx
sudo systemctl enable nginx

# CentOS/RHEL
sudo yum install nginx
sudo systemctl enable nginx
```

**复制构建产物：**
```bash
sudo mkdir -p /var/www/football
sudo cp -r dist/* /var/www/football/
sudo chown -R nginx:nginx /var/www/football   # CentOS 使用 nginx 用户
sudo chown -R www-data:www-data /var/www/football  # Debian/Ubuntu 使用 www-data 用户
```

**创建 Nginx 配置文件：**

```bash
sudo tee /etc/nginx/conf.d/football.conf << 'EOF'
server {
    listen 80;
    server_name your-domain.com;          # 替换为实际域名或 IP

    # 前端静态资源
    root /var/www/football;
    index index.html;

    # 开启 gzip 压缩
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml text/javascript;
    gzip_min_length 1k;
    gzip_comp_level 6;

    # 静态资源缓存（带 hash 的文件可以长期缓存）
    location /assets/ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # Vue Router history 模式：所有前端路由回退到 index.html
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 禁止访问隐藏文件
    location ~ /\. {
        deny all;
    }

    # 错误页面
    error_page 500 502 503 504 /50x.html;
}
EOF
```

**检查并重载 Nginx：**
```bash
# 检查配置语法
sudo nginx -t

# 语法正确则重载
sudo systemctl reload nginx

# 确保 Nginx 开机自启
sudo systemctl enable nginx
```

#### 方式二：集成到 Spring Boot（简易部署）

如果不使用 Nginx，可以将前端构建产物集成到后端 JAR 包中：

```bash
# 1. 前端构建
cd frontend && npm run build

# 2. 将 dist 复制到 Spring Boot 静态资源目录
mkdir -p ../backend/src/main/resources/static
cp -r dist/* ../backend/src/main/resources/static/

# 3. 后端配置添加静态资源路径（可选，Spring Boot 默认支持）
# 无需额外配置，Spring Boot 会自动服务 static/ 下的文件

# 4. 重新打包
cd ../backend && mvn clean package -DskipTests

# 5. 启动（单一 JAR 包运行所有内容）
java -jar target/football-backend-1.0.0.jar -Dspring.profiles.active=prod
```

> ⚠️ 这种方式下，由于前端请求地址为 `http://localhost:8080/api`（绝对路径），与后端同域同端口，不存在跨域问题，请求直接由 Spring Boot 处理。

---

### 4.3 防火墙配置

```bash
# 开放 HTTP 端口
sudo firewall-cmd --permanent --add-service=http    # CentOS/RHEL
sudo ufw allow 80/tcp                               # Ubuntu/Debian

# 如需开放后端端口（调试用，生产不建议暴露外网）
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo ufw allow 8080/tcp
```

---

### 4.4 验证前端

浏览器访问 `http://your-server-ip` 或 `http://your-domain.com`，应能看到：

1. 首页比赛列表正常展示
2. 点击比赛卡片可进入详情页
3. 详情页四个 Tab（阵容/技战术/Kelly/赔率对比）均可正常切换
4. 联赛筛选、分页功能正常

---

## 5. 完整部署检查清单

| # | 检查项 | 命令/操作 | 预期结果 |
|---|--------|-----------|----------|
| 1 | JDK 21 已安装 | `java -version` | 显示 "21" |
| 2 | Maven 已安装 | `mvn -version` | 显示 3.9+ |
| 3 | Node.js 20+ 已安装 | `node -v` | v20.x.x |
| 4 | PostgreSQL 运行中 | `systemctl status postgresql` | active (running) |
| 5 | 数据库 football 已创建 | `psql -U postgres -l` | 列表含 football |
| 6 | 后端编译成功 | `cd backend && mvn clean package -DskipTests` | BUILD SUCCESS |
| 7 | 环境变量 DB_PASSWORD 已设置 | `echo $DB_PASSWORD` | 输出密码 |
| 8 | 后端 systemd 服务运行中 | `systemctl status football-backend` | active (running) |
| 9 | 后端 API 可访问 | `curl http://localhost:8080/api/matches/leagues` | 返回 JSON |
| 10 | 前端构建成功 | `cd frontend && npm run build` | dist/ 目录生成 |
| 11 | 前端文件已部署 | `ls /var/www/football/index.html` | 文件存在 |
| 12 | Nginx 配置正确 | `nginx -t` | syntax is ok |
| 13 | Nginx 运行中 | `systemctl status nginx` | active (running) |
| 14 | 浏览器可访问 | 打开 `http://your-server-ip` | 页面正常展示 |

---

## 6. 常见问题排查

### Q1：后端启动报错 "Failed to configure a DataSource"

**原因**：数据库未启动、密码错误、数据库不存在或端口被占用。

**排查：**
```bash
# 确认 PostgreSQL 运行中
systemctl status postgresql

# 确认可连接
psql -h localhost -U postgres -d football -W

# 查看 JPA 详细日志
java -jar football-backend-1.0.0.jar \
  -Dspring.profiles.active=prod \
  -DDB_PASSWORD=your_secure_password \
  --logging.level.org.hibernate.SQL=TRACE
```

### Q2：前端页面空白，控制台报 404

**原因**：Vue Router history 模式需要服务端配置回退规则。

**解决**：确认 Nginx 配置中包含 `try_files $uri $uri/ /index.html;`。

### Q3：前端 API 请求失败 / 网络错误

**原因**：后端未启动或端口被占用，前端直接调用 `http://localhost:8080/api` 无法连接。

**排查：**
```bash
# 确认后端端口监听
netstat -tlnp | grep 8080

# 确认后端服务状态
sudo systemctl status football-backend

# 直接测试后端接口
curl http://localhost:8080/api/matches/leagues
```

### Q4：跨域错误（CORS）

**原因**：后端 CORS 配置未生效。

**解决**：检查 `backend/src/main/java/com/football/config/WebConfig.java` 是否允许前端域名。默认配置允许所有来源（`*`），如果修改过需确认。

### Q5：生产环境没有数据

**原因**：`DataInitializer` 仅在 `dev` profile 下运行（`@Profile("dev")`），生产环境不会自动填充 Mock 数据。

**解决**：生产环境需要接入真实数据源。开发调试时可临时切换 profile：

```bash
# 仅用于初次数据填充，不要长期在生产环境使用
java -jar football-backend-1.0.0.jar \
  -Dspring.profiles.active=dev \
  -Dspring.jpa.hibernate.ddl-auto=update
```

### Q6：内存不足导致 OOM

**解决**：调整 JVM 堆内存参数。

```bash
# 修改 systemd 服务文件中的 ExecStart 行
-Xms1g -Xmx2g    # 服务器内存 4GB 以上建议
```

### Q7：PostgreSQL 允许外部连接

**配置**（仅开发/测试环境）：

编辑 `postgresql.conf`：
```
listen_addresses = '*'   # 默认是 localhost
```

编辑 `pg_hba.conf`，添加：
```
host    all    all    0.0.0.0/0    md5   # 或指定 IP 段
```

然后 `systemctl restart postgresql`。

> ⚠️ 生产环境请使用白名单 IP 限制，不要开放 `0.0.0.0/0`。

---

## 7. 运维建议

### 6.1 日志管理

```bash
# 查看后端实时日志
sudo journalctl -u football-backend -f --since "10 min ago"

# 查看 Nginx 访问日志
tail -f /var/log/nginx/access.log

# 查看 Nginx 错误日志
tail -f /var/log/nginx/error.log
```

### 6.2 备份数据库

```bash
# 完整备份
pg_dump -U postgres -d football -F c -f football_$(date +%Y%m%d).dump

# 仅备份数据（不含表结构）
pg_dump -U postgres -d football --data-only -F p -f football_data_$(date +%Y%m%d).sql
```

**设置 cron 定时备份：**
```bash
# 每天凌晨 2 点备份
0 2 * * * pg_dump -U postgres -d football -F c -f /backups/football_$(date +\%Y\%m\%d).dump
```

### 6.3 健康监控

建议监控以下端点：
- `GET http://localhost:8080/api/matches?limit=1` — 后端可用性
- `GET http://localhost/swagger-ui.html` — Swagger 文档可访问
- PostgreSQL 端口 5432 连通性

### 6.4 性能优化建议

| 项目 | 建议 |
|------|------|
| 数据库连接池 | 生产环境将 HikariCP 连接池设为 20-50 连接 |
| Nginx 缓存 | 对 `/api/matches/leagues` 这类低频变化接口加 proxy_cache |
| CDN | 将 `/assets/` 静态资源推送到 CDN |
| HTTPS | 使用 Let's Encrypt 免费证书配置 SSL |

### 6.5 更新部署流程

```bash
# 1. 构建新版本
cd backend && mvn clean package -DskipTests
cd ../frontend && npm run build

# 2. 停止服务
sudo systemctl stop football-backend

# 3. 替换文件
sudo cp backend/target/football-backend-1.0.0.jar /opt/football/backend/
sudo cp -r frontend/dist/* /var/www/football/

# 4. 启动服务
sudo systemctl start football-backend
sudo systemctl reload nginx

# 5. 验证
curl -s http://localhost:8080/api/matches?limit=1 | head -c 200
```

---

## 附录 A：配置文件参考

### application.yml（通用配置）

```yaml
server:
  port: 8080

spring:
  profiles:
    active: dev                    # 默认开发环境

  jackson:
    time-zone: Asia/Shanghai       # 时区：东八区
    date-format: yyyy-MM-dd HH:mm:ss
    default-property-inclusion: non_null  # 不序列化 null 字段

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
    password: ${DB_PASSWORD:postgres}    # 通过环境变量注入

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
```

### 前端 API 请求配置

```typescript
// frontend/src/api/request.ts
import axios from 'axios'

/**
 * API 地址读取优先级：
 *   1. 运行时配置（public/config.js，部署后可修改）
 *   2. 构建时注入的环境变量（VITE_API_BASE_URL）
 *   3. 兜底默认值
 */
const getApiBaseUrl = (): string => {
  if (window.__APP_CONFIG__?.API_BASE_URL) {
    return window.__APP_CONFIG__.API_BASE_URL
  }
  if (import.meta.env.VITE_API_BASE_URL) {
    return import.meta.env.VITE_API_BASE_URL
  }
  return 'http://localhost:8080/api'
}

const request = axios.create({
  baseURL: getApiBaseUrl(),
  timeout: 10000,
})
```

> **说明**：前端支持构建时注入 + 运行时覆盖双重配置。详见[第 3 章：前端独立部署配置](#3-前端独立部署配置)。

---

## 附录 B：API 接口清单

| 接口 | 方法 | 参数 | 说明 |
|------|------|------|------|
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

完整 API 文档：启动后端后访问 `http://localhost:8080/swagger-ui.html`

---

> **文档版本**: 1.0  
> **最后更新**: 2026-06-18  
> **维护团队**: 如有问题请参考项目根目录 `README.md`
