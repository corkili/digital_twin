#!/bin/bash

# 数字孪生项目构建脚本
# 用于编译和打包项目

set -e  # 遇到错误立即退出

echo "=== 数字孪生项目构建脚本 ==="
echo ""

# 检测操作系统
OS="$(uname -s)"
case "${OS}" in
    Linux*)     PLATFORM=Linux;;
    Darwin*)    PLATFORM=Mac;;
    CYGWIN*)    PLATFORM=Cygwin;;
    MINGW*)     PLATFORM=MinGw;;
    *)          PLATFORM="UNKNOWN:${OS}"
esac
echo "💻 操作系统: ${PLATFORM}"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查Java环境
echo "🔍 检查Java环境..."
if ! command -v java &> /dev/null; then
    echo -e "${RED}❌ Java未安装${NC}"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 11 ]; then
    echo -e "${RED}❌ Java版本过低，需要Java 11或更高版本${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Java版本: $(java -version 2>&1 | head -n 1)${NC}"

# 检查Maven环境
echo "🔍 检查Maven环境..."
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}❌ Maven未安装${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Maven版本: $(mvn -version | head -n 1)${NC}"

# 清理之前的构建
echo "🧹 清理之前的构建..."
mvn clean

# 编译项目
echo "🔨 编译项目..."
mvn compile

# 运行测试（可选）
echo "🧪 运行测试..."
if mvn test; then
    echo -e "${GREEN}✅ 测试通过${NC}"
else
    echo -e "${YELLOW}⚠️  测试失败，但仍继续构建${NC}"
fi

# 打包项目
echo "📦 打包项目..."
mvn package -DskipTests

# 检查构建结果
if [ -f "target/websocket-server-1.0.0.jar" ]; then
    JAR_SIZE=$(du -h target/websocket-server-1.0.0.jar | cut -f1)
    echo -e "${GREEN}✅ 构建成功！${NC}"
    echo "📊 JAR文件大小: $JAR_SIZE"
    echo "📁 JAR文件位置: target/websocket-server-1.0.0.jar"
else
    echo -e "${RED}❌ 构建失败，未找到JAR文件${NC}"
    exit 1
fi

cp target/websocket-server-1.0.0.jar deploy/

echo ""
echo "🎉 构建完成！"
echo "💡 使用 ./run.sh 启动项目"