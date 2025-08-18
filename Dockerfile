# 基于 Ubuntu 22.04
FROM crpi-7x0z5py955uoyio4.cn-hangzhou.personal.cr.aliyuncs.com/corkili/linux_amd64_openjdk:11-jdk-slim

# 创建工作目录
RUN mkdir -p /app/dt_server/deploy
WORKDIR /app/dt_server/deploy

# 拷贝本地项目到镜像
COPY ./deploy/websocket-server-1.0.0.jar ./deploy/run.sh /app/dt_server/deploy/

# 设置默认端口环境变量
ENV SERVER_PORT=8081

# 设置容器启动命令（默认后台启动，使用环境变量端口）
CMD ["/bin/bash", "-c", "./run.sh -p ${SERVER_PORT:-8081}"]
# CMD ["/bin/bash", "-c", "cd /app/dt_server"]