#!/bin/bash

# pack_deploy.sh - 打包部署文件

# 检查Docker是否已安装
if ! command -v docker &> /dev/null; then
    echo "错误: Docker未安装或未在PATH中！"
    exit 1
fi

# 检查dt_server:latest镜像是否存在
if ! docker image inspect dt_server:latest &> /dev/null; then
    echo "错误: dt_server:latest镜像不存在！"
    echo "请确保已构建dt_server:latest镜像"
    exit 1
fi

# 检查上级目录的docker-compose.yml是否存在
if [ ! -f "../docker-compose.yml" ]; then
    echo "错误: ../docker-compose.yml文件不存在！"
    exit 1
fi

# 检查当前脚本文件是否存在
for script in "start_docker_server.sh" "load_docker_image.sh"; do
    if [ ! -f "$script" ]; then
        echo "错误: $script文件不存在！"
        exit 1
    fi
done

echo "正在打包部署文件..."

# 创建dt_server_image文件夹
rm -rf dt_server_image
mkdir dt_server_image

# 保存Docker镜像
echo "正在保存Docker镜像..."
docker save -o dt_server.tar dt_server:latest

# 复制文件到dt_server_image文件夹
echo "正在复制文件..."
cp dt_server.tar dt_server_image/
cp ../docker-compose.yml dt_server_image/
cp start_docker_server.sh dt_server_image/
cp load_docker_image.sh dt_server_image/

# 打包成tar.gz格式
echo "正在创建压缩包..."
tar -czf dt_server_image.tar.gz dt_server_image/

# 检查打包是否成功
if [ $? -eq 0 ]; then
    echo "打包成功！"
    echo "生成的文件: dt_server_image.tar.gz"
    echo "包含内容:"
    echo "  - dt_server.tar (Docker镜像)"
    echo "  - docker-compose.yml"
    echo "  - start_docker_server.sh"
    echo "  - load_docker_image.sh"
    
    # 清理临时文件
    rm -f dt_server.tar
    rm -rf dt_server_image
    
    echo "临时文件已清理"
else
    echo "错误: 打包失败！"
    exit 1
fi