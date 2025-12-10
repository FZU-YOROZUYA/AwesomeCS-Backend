# 第一阶段：构建阶段
# 使用 DaoCloud 镜像加速，解决 docker.io 连接问题
FROM m.daocloud.io/docker.io/library/maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# 复制 pom.xml 并预下载依赖（利用 Docker 缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 构建项目，跳过测试
RUN mvn clean package -DskipTests

# 第二阶段：运行阶段
# 使用 DaoCloud 镜像加速
FROM m.daocloud.io/docker.io/library/eclipse-temurin:17-jre-jammy
WORKDIR /app

# 从构建阶段复制生成的 JAR 文件
# 注意：这里假设生成的 jar 包名称为 AwesomeCS-0.0.1-SNAPSHOT.jar，这取决于 pom.xml 中的 artifactId 和 version
COPY --from=build /app/target/AwesomeCS-0.0.1-SNAPSHOT.jar app.jar

# 暴露应用端口 (根据 application-dev.yml 配置为 8085)
EXPOSE 8085

# 设置容器启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]
