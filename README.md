# AwesomeCS-Backend

AwesomeCS 的后端部分

## 启动方法

## 1. 单独作为后端使用

如果想将本项目单独部署（不和 AwesomeCS-Front 交互），可以使用 Docker，这里给出两种 `Dockerfile` 供使用：

1. 使用 `Dcokerfile` 文件构建项目，需要良好的网络情况，整个项目从头开始编译。
```bash
docker build -t awesomec-backend -f ./Dockerfile .
```

2. 使用 `Dockerfile.local` 构建项目，先在本地环境使用 maven 预编译，直接使用预编译后的 jar 包进行部署

```bash
docker build -t awesomec-backend -f ./Dockerfile.local . 
```

# 2. Docker-Compose （与 AwesomeCS-Front 一起）

参考 [主仓库](https://github.com/FZU-YOROZUYA/SE-Project/tree/main/awesome_cs) 下的 README