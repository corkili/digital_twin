#!/bin/bash

# 数字孪生项目运行脚本
# 用于启动Spring Boot应用

# 设置默认在前台运行
BACKGROUND=false
PORT=8081
PROFILE=""

# 解析命令行参数
while [[ $# -gt 0 ]]; do
    case $1 in
        -b|--background)
            BACKGROUND=true
            shift
            ;;
        -p|--port)
            if [[ -n "$2" ]]; then
                PORT="$2"
                shift 2
            else
                echo "错误: -p 参数需要指定端口号"
                exit 1
            fi
            ;;
        --profile)
            if [[ -n "$2" ]]; then
                PROFILE="$2"
                shift 2
            else
                echo "错误: --profile 参数需要指定配置文件名"
                exit 1
            fi
            ;;
        -h|--help)
            echo "使用方法: $0 [选项]"
            echo "选项:"
            echo "  -b, --background  在后台启动应用"
            echo "  -p, --port <端口> 指定服务启动端口(默认: 8081)"
            echo "  --profile <名称>  指定Spring配置文件(如: test, dev, prod)"
            echo "  -h, --help        显示帮助信息"
            exit 0
            ;;
        *)
            echo "未知参数: $1"
            echo "使用 -h 或 --help 查看帮助信息"
            exit 1
            ;;
    esac
done

# 检测操作系统
OS="$(uname -s)"
case "${OS}" in
    Linux*)     PLATFORM=Linux;;
    Darwin*)    PLATFORM=Mac;;
    CYGWIN*)    PLATFORM=Cygwin;;
    MINGW*)     PLATFORM=MinGw;;
    *)          PLATFORM="UNKNOWN:${OS}"
esac

echo "=== 数字孪生项目运行脚本 ==="
echo ""
echo "💻 操作系统: ${PLATFORM}"
echo "🔌 端口: $PORT"
if [ -n "$PROFILE" ]; then
    echo "⚙️  配置文件: $PROFILE"
fi
if [ "$BACKGROUND" = true ]; then
    echo "🔄 启动模式: 后台运行"
else
    echo "🔄 启动模式: 前台运行"
fi
echo ""

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 检查JAR文件是否存在
JAR_FILE="websocket-server-1.0.0.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo -e "${YELLOW}⚠️  未找到JAR文件，请先运行构建脚本${NC}"
    echo "运行: ./build.sh"
    exit 1
fi

# 设置JVM参数
JVM_OPTS="-Xmx512m -Xms256m"

# 创建日志目录
LOG_DIR="logs"
if [ ! -d "$LOG_DIR" ]; then
    mkdir -p "$LOG_DIR"
fi

# 设置日志文件
LOG_FILE="$LOG_DIR/digital-twin-websocket.log"

# 构建Java启动参数
JAVA_ARGS="--server.port=$PORT"
if [ -n "$PROFILE" ]; then
    JAVA_ARGS="$JAVA_ARGS --spring.profiles.active=$PROFILE"
fi

# 启动应用
echo "🚀 启动Spring Boot应用..."
echo "📊 JAR文件: $JAR_FILE"
echo "🔗 访问地址: http://localhost:$PORT/api"
echo "📡 WebSocket端点: ws://localhost:$PORT/api/ws"
if [ -n "$PROFILE" ]; then
    echo "⚙️  激活配置: $PROFILE"
fi
echo ""

# 检查后台启动模式
if [ "$BACKGROUND" = true ]; then
    echo "📋 日志将输出到: $LOG_FILE"
    echo "🔄 应用正在后台启动..."
    
    # 后台启动并记录日志
    nohup java $JVM_OPTS -jar $JAR_FILE $JAVA_ARGS > "$LOG_FILE" 2>&1 &
    APP_PID=$!
    
    echo -e "${GREEN}✅ 应用启动成功！${NC}"
    echo "📝 进程ID: $APP_PID"
    echo "📋 日志文件: $LOG_FILE"
    echo ""
    echo "查看日志:"
    echo "  tail -f $LOG_FILE"
    echo ""
    echo "停止应用:"
    echo "  kill $APP_PID"
else
    echo "📋 日志输出:"
    echo ""
    
    # 前台启动并输出日志
    java $JVM_OPTS -jar $JAR_FILE $JAVA_ARGS
    
    # 如果应用意外退出，显示提示信息
    echo ""
    echo -e "${RED}❌ 应用已停止${NC}"
    echo "可能的原因:"
    echo "  1. 端口被占用"
    echo "  2. RabbitMQ连接失败"
    echo "  3. 配置错误"
    echo ""
    echo "查看日志文件: $LOG_FILE"
fi