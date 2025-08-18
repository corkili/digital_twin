#!/bin/bash

# 数字孪生项目运行脚本
# 用于启动Spring Boot应用

# 设置默认在前台运行
BACKGROUND=false
PORT=8081

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
        -h|--help)
            echo "使用方法: $0 [选项]"
            echo "选项:"
            echo "  -b, --background  在后台启动应用"
            echo "  -p, --port <端口> 指定服务启动端口(默认: 8081)"
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

# 检查端口占用

# 跨平台端口检测函数
check_port() {
    local port=$1
    
    if command -v lsof &> /dev/null; then
        # macOS和Linux都支持lsof
        lsof -i :$port > /dev/null 2>&1
    elif command -v netstat &> /dev/null; then
        # Linux备选方案
        netstat -tuln | grep ":$port" > /dev/null 2>&1
    elif command -v ss &> /dev/null; then
        # Linux新工具
        ss -tuln | grep ":$port" > /dev/null 2>&1
    else
        echo -e "${YELLOW}⚠️  未找到端口检测工具${NC}"
        return 1
    fi
}

get_pid_by_port() {
    local port=$1
    
    if command -v lsof &> /dev/null; then
        lsof -t -i :$port 2>/dev/null
    else
        # 其他平台的简化版本
        echo ""
    fi
}

if check_port $PORT; then
    echo -e "${YELLOW}⚠️  端口 $PORT 已被占用${NC}"
    
    if command -v lsof &> /dev/null; then
        echo "占用进程:"
        lsof -i :$PORT
    fi
    
    echo ""
    read -p "是否强制终止占用进程？(y/n): " -n 1 -r
    echo ""
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        PID=$(get_pid_by_port $PORT)
        if [ -n "$PID" ]; then
            if kill -9 $PID 2>/dev/null; then
                sleep 2
                echo -e "${GREEN}✅ 已终止占用进程${NC}"
            else
                echo -e "${RED}❌ 无法终止进程，可能需要sudo权限${NC}"
                exit 1
            fi
        else
            echo -e "${YELLOW}⚠️  无法获取进程PID，请手动处理${NC}"
            exit 1
        fi
    else
        echo "请手动释放端口后再运行"
        exit 1
    fi
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

# 启动应用
echo "🚀 启动Spring Boot应用..."
echo "📊 JAR文件: $JAR_FILE"
echo "🔗 访问地址: http://localhost:$PORT/api"
echo "📡 WebSocket端点: ws://localhost:$PORT/api/ws"
echo ""

# 检查后台启动模式
if [ "$BACKGROUND" = true ]; then
    echo "📋 日志将输出到: $LOG_FILE"
    echo "🔄 应用正在后台启动..."
    
    # 后台启动并记录日志
    nohup java $JVM_OPTS -jar $JAR_FILE --server.port=$PORT > "$LOG_FILE" 2>&1 &
    APP_PID=$!
    
    # 等待应用启动
    sleep 5
    
    # 检查应用是否成功启动
    if ps -p $APP_PID > /dev/null 2>&1; then
        echo -e "${GREEN}✅ 应用启动成功！${NC}"
        echo "📝 进程ID: $APP_PID"
        echo "📋 日志文件: $LOG_FILE"
        echo ""
        echo "查看日志:"
        echo "  tail -f $LOG_FILE"
        echo ""
        echo "停止应用:"
        echo "  kill $APP_PID"
        
        # 将PID写入文件以便后续管理
        echo $APP_PID > "$LOG_DIR/app.pid"
    else
        echo -e "${RED}❌ 应用启动失败！${NC}"
        echo "📋 请查看日志文件: $LOG_FILE"
        exit 1
    fi
else
    echo "📋 日志输出:"
    echo ""
    
    # 前台启动并输出日志
    java $JVM_OPTS -jar $JAR_FILE --server.port=$PORT
    
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