# ====================== 阶段1：Maven 构建多模块项目 ======================
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# 分层复制所有模块pom，缓存依赖（核心加速逻辑）
COPY pom.xml .
COPY orchard-common/pom.xml orchard-common/
COPY orchard-service/pom.xml orchard-service/
COPY orchard-userManagement/pom.xml orchard-userManagement/
COPY orchard-app/pom.xml orchard-app/
COPY orchard-ai/pom.xml orchard-ai/
COPY orchard-auth/pom.xml orchard-auth/
COPY orchard-file/pom.xml orchard-file/

# 提前拉取全部依赖，离线构建
RUN mvn dependency:go-offline -B

# 复制完整源码
COPY . .

# 构建参数：指定要打包的微服务模块，默认 orchard-service
ARG MODULE=orchard-service
# -pl 指定模块，-am 自动构建该模块依赖的子模块（common等）
RUN mvn clean package -pl ${MODULE} -am -DskipTests -B

# ====================== 阶段2：轻量运行镜像 ======================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 构建参数必须重新声明（ARG仅当前阶段生效）
ARG MODULE=orchard-service

# 1. 安装时区包，设置东八区（解决日志时间不对）
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

# 2. 创建专用运行用户，禁止root启动
RUN addgroup -S orchard && adduser -S orchard -G orchard

# 3. 复制打包好的Jar
COPY --from=builder /app/${MODULE}/target/*.jar app.jar

# 4. 日志持久化目录 + 权限分配
RUN mkdir -p /app/logs && chown -R orchard:orchard /app
VOLUME ["/app/logs"]

# 切换普通用户运行
USER orchard

EXPOSE 48080

# 健康检查：监控SpringBoot服务存活
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -q --spider http://127.0.0.1:48080/actuator/health || exit 1

# 改为CMD，支持外部传入JVM参数、spring环境参数
ENTRYPOINT ["java"]
CMD ["-jar", "app.jar", "--spring.profiles.active=prod"]

# 运行时需要传入环境变量，例如：
# docker run --env-file /home/www/orchard_aigc_admin/.env ...
# 或者：docker run -e MYSQL_HOST=xxx -e MYSQL_USERNAME=xxx ...