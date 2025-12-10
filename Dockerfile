##########
# Builder #
##########
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

# 预拉依赖，加快后续构建
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline

# 拷贝源码并打包
COPY src ./src
RUN mvn -B -ntp package -DskipTests

##########
# Runner  #
##########
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# 复制构建产物（使用通配以避免硬编码版本号）
COPY --from=builder /build/target/*SNAPSHOT.jar app.jar

EXPOSE 8085
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
