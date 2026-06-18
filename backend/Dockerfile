# ============================================================
# 后端 Dockerfile — 多阶段构建
# 阶段1: Maven 构建
# 阶段2: JRE 运行
# ============================================================

# ---------- 阶段1: Maven 构建 ----------
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# 先复制 pom.xml 以利用 Docker 缓存层
COPY pom.xml .
RUN mvn dependency:go-offline -B -q 2>/dev/null || true

# 复制源码并打包
COPY src ./src
RUN mvn clean package -DskipTests -q

# ---------- 阶段2: JRE 运行 ----------
FROM eclipse-temurin:21-jre-alpine

# 创建非 root 用户
RUN addgroup -g 1000 appgroup && \
    adduser -u 1000 -G appgroup -D appuser

WORKDIR /app

# 从构建阶段复制 JAR
COPY --from=builder /app/target/football-backend-1.0.0.jar ./app.jar

# 创建日志目录
RUN mkdir -p /app/logs && chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=5 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/health || exit 1

# JVM 参数可通过 JAVA_OPTS 环境变量覆盖
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS:--Xms256m -Xmx512m} -jar app.jar"]
