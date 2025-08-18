#!/bin/bash

# load_docker_image.sh - 加载Docker镜像

# 检查dt_server.tar文件是否存在
if [ ! -f "dt_server.tar" ]; then
    echo "错误: dt_server.tar文件不存在！"
    echo "请确保dt_server.tar文件在当前目录下。"
    exit 1
fi

# 检查Docker是否已安装
if ! command -v docker &> /dev/null; then
    echo "错误: Docker未安装或未在PATH中！"
    exit 1
fi

echo "正在加载Docker镜像..."
echo "镜像文件: dt_server.tar"

# 加载Docker镜像
docker load -i dt_server.tar

# 检查加载是否成功
if [ $? -eq 0 ]; then
    echo "Docker镜像加载成功！"
    echo "使用 'docker images' 查看已加载的镜像"
else
    echo "错误: Docker镜像加载失败！"
    exit 1
fi