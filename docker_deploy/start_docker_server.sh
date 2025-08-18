#!/bin/bash

# start_docker_server.sh - 启动Docker容器

# 默认端口
SERVER_PORT=8082

# 解析命令行参数
while [[ $# -gt 0 ]]; do
    case $1 in
        -p|--port)
            if [[ -n $2 ]] && [[ $2 =~ ^[0-9]+$ ]]; then
                SERVER_PORT=$2
                shift 2
            else
                echo "错误: -p 参数需要一个有效的端口号"
                echo "用法: $0 [-p 端口号]"
                exit 1
            fi
            ;;
        -h|--help)
            echo "用法: $0 [-p 端口号]"
            echo "选项:"
            echo "  -p, --port    指定服务端口 (默认: 8082)"
            echo "  -h, --help    显示帮助信息"
            exit 0
            ;;
        *)
            echo "错误: 未知参数 '$1'"
            echo "用法: $0 [-p 端口号]"
            exit 1
            ;;
    esac
done

# 检查docker-compose.yml文件是否存在
if [ ! -f "docker-compose.yml" ]; then
    echo "错误: docker-compose.yml文件不存在！"
    echo "请确保docker-compose.yml文件在当前目录下。"
    exit 1
fi

# 检查Docker是否已安装
if ! command -v docker &> /dev/null; then
    echo "错误: Docker未安装或未在PATH中！"
    exit 1
fi

# 检查docker-compose是否可用
if ! command -v docker-compose &> /dev/null; then
    echo "错误: docker-compose未安装或未在PATH中！"
    exit 1
fi

echo "正在启动Docker容器..."
echo "使用端口: $SERVER_PORT"

# 启动Docker容器
SERVER_PORT=$SERVER_PORT docker-compose up -d

# 检查启动是否成功
if [ $? -eq 0 ]; then
    echo "Docker容器启动成功！"
    echo "服务将在端口$SERVER_PORT上运行"
    echo "使用 'docker-compose ps' 查看容器状态"
    echo "使用 'docker-compose logs -f' 查看日志"
else
    echo "错误: Docker容器启动失败！"
    exit 1
fi