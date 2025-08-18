# 数字孪生项目本地开发指南

## 🚀 快速开始

### 1. 环境准备
确保已安装以下软件：
- Java 11 或更高版本
- Maven 3.6+
- RabbitMQ (通过Homebrew安装)

### 2. 启动RabbitMQ
```bash
# 启动RabbitMQ服务
brew services start rabbitmq

# 初始化RabbitMQ配置
./init-rabbitmq.sh
```

### 3. 构建项目
```bash
# 编译并打包项目
./build.sh
```

### 4. 运行项目
```bash
# 启动Spring Boot应用
./run.sh
```

## 📋 脚本说明

### build.sh
- 检查Java和Maven环境
- 清理之前的构建
- 编译源代码
- 运行单元测试
- 打包生成JAR文件

### run.sh
- 检查RabbitMQ服务状态
- 检查端口占用情况
- 启动Spring Boot应用
- 显示访问地址和日志

### init-rabbitmq.sh
- 创建所需的交换机
- 创建消息队列
- 绑定队列到交换机
- 设置用户权限

### test-local.sh
- 检查服务状态
- 显示访问地址
- 提供测试命令示例

## 🔗 访问地址

- **WebSocket端点**: `ws://localhost:8081/api/ws`
- **HTTP API**: `http://localhost:8081/api`
- **健康检查**: `http://localhost:8081/api/actuator/health`
- **RabbitMQ管理**: `http://localhost:15672` (guest/guest)

## 🧪 测试示例

### 发送测试消息
```bash
# 使用RabbitMQ管理工具发送消息
rabbitmqadmin -H localhost -u guest -p guest publish \
  routing_key=sensor.data \
  payload='{"temperature":25.5,"humidity":60,"timestamp":"2024-01-01T12:00:00"}'
```

### 使用WebSocket客户端连接
```bash
# 安装websocat (WebSocket客户端)
brew install websocat

# 连接到WebSocket
websocat ws://localhost:8081/api/ws

# 订阅主题
SUBSCRIBE
id:sub-0
destination:/topic/sensor-data

^@
```

## 📊 监控和日志

### 查看应用日志
```bash
# 实时查看日志
tail -f logs/digital-twin-websocket.log

# 查看最后100行日志
tail -n 100 logs/digital-twin-websocket.log
```

### 检查RabbitMQ状态
```bash
# 查看队列状态
rabbitmqadmin -H localhost -u guest -p guest list queues

# 查看连接
rabbitmqadmin -H localhost -u guest -p guest list connections
```

## 🛠️ 故障排除

### 端口被占用
如果8081端口被占用，run.sh会提示并询问是否终止占用进程。

### RabbitMQ连接失败
确保RabbitMQ已启动：
```bash
brew services start rabbitmq
```

### 构建失败
检查Java版本：
```bash
java -version
mvn -version
```

## 🔄 重新启动流程
```bash
# 1. 停止应用 (Ctrl+C)
# 2. 重新构建
./build.sh
# 3. 重新启动
./run.sh
```