# 数字孪生WebSocket服务器

基于Spring Boot的WebSocket服务器，用于实时推送从RabbitMQ消费的传感器数据到Web前端。

## 功能特性

- ✅ 消费RabbitMQ中的JSON格式传感器数据
- ✅ 通过WebSocket实时推送到前端
- ✅ 确保按消费顺序推送数据
- ✅ 仅在建立WebSocket连接时推送
- ✅ 支持SockJS回退方案
- ✅ RESTful健康检查接口

## 技术栈

- **Java**: 11
- **Spring Boot**: 2.7.18 (兼容Java 11的最新版本)
- **Spring WebSocket**: STOMP协议支持
- **Spring AMQP**: RabbitMQ集成
- **Jackson**: JSON序列化/反序列化
- **Lombok**: 简化代码

## 快速开始

### 1. 环境准备

确保已安装：
- Java 11 (JDK)
- Maven 3.6+
- RabbitMQ 3.8+

### 2. 配置RabbitMQ

```bash
# 启动RabbitMQ服务
rabbitmq-server

# 创建交换器和队列（可选，程序会自动创建）
rabbitmqadmin declare exchange name=sensor.data.exchange type=topic durable=true
rabbitmqadmin declare queue name=sensor.data.queue durable=true
rabbitmqadmin declare binding source=sensor.data.exchange destination=sensor.data.queue routing_key=sensor.data
```

### 3. 构建和运行

```bash
# 克隆项目
cd digital_twin/server

# 构建项目
mvn clean package

# 运行应用
mvn spring-boot:run

# 或者运行打包后的jar
java -jar target/websocket-server-1.0.0.jar
```

### 4. 测试WebSocket连接

1. 打开浏览器访问: http://localhost:8081/api/
2. 点击"连接WebSocket"按钮
3. 使用以下命令发送测试数据到RabbitMQ:

```bash
# 使用RabbitMQ CLI
rabbitmqadmin publish routing_key="sensor.data" \
  payload='{"sensorId":"TEMP-001","timestamp":"2024-01-01T12:00:00","value":25.5,"unit":"°C","location":"Room-101","status":"normal"}'

# 或者使用curl
curl -X POST http://localhost:15672/api/exchanges/%2f/sensor.data.exchange/publish \
  -u guest:guest \
  -H "content-type: application/json" \
  -d '{"properties":{},"routing_key":"sensor.data","payload":"{\"sensorId\":\"TEMP-001\",\"timestamp\":\"2024-01-01T12:00:00\",\"value\":25.5,\"unit\":\"°C\",\"location\":\"Room-101\",\"status\":\"normal\"}","payload_encoding":"string"}'
```

## API接口

### WebSocket端点
- **连接地址**: `ws://localhost:8081/api/ws`
- **订阅主题**: `/topic/sensor-data`
- **应用前缀**: `/app`

### REST接口
- **健康检查**: `GET http://localhost:8081/api/api/health`
- **WebSocket信息**: `GET http://localhost:8081/api/api/health/websocket`

## 数据模型

### 传感器数据 (SensorData)
```json
{
  "sensorId": "TEMP-001",
  "timestamp": "2024-01-01T12:00:00",
  "value": 25.5,
  "unit": "°C",
  "location": "Room-101",
  "status": "normal"
}
```

### WebSocket响应格式
```json
{
  "code": 200,
  "message": "success",
  "timestamp": "2024-01-01T12:00:00",
  "data": {
    // 传感器数据对象
  }
}
```

## 配置说明

### 应用配置 (application.yml)

```yaml
server:
  port: 8081
  context-path: /api

spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

### 环境变量

可以通过环境变量覆盖配置：

```bash
export RABBITMQ_HOST=your-rabbitmq-host
export RABBITMQ_PORT=5672
export RABBITMQ_USERNAME=your-username
export RABBITMQ_PASSWORD=your-password
```

## 开发指南

### 项目结构
```
src/main/java/com/digitaltwin/websocket/
├── config/          # 配置类
│   ├── RabbitMQConfig.java
│   └── WebSocketConfig.java
├── controller/      # 控制器
│   ├── HealthController.java
│   └── WebSocketController.java
├── model/          # 数据模型
│   ├── SensorData.java
│   └── WebSocketResponse.java
├── service/        # 业务服务
│   ├── RabbitMQConsumerService.java
│   └── WebSocketPushService.java
└── DigitalTwinWebsocketApplication.java
```

### 添加新的传感器类型

1. 创建新的数据模型类继承SensorData
2. 在RabbitMQConsumerService中添加对应的监听器
3. 更新前端测试页面

## 故障排除

### 常见问题

1. **连接RabbitMQ失败**
   - 检查RabbitMQ服务是否运行
   - 验证用户名密码是否正确
   - 检查防火墙设置

2. **WebSocket连接失败**
   - 检查端口8081是否被占用
   - 验证浏览器是否支持WebSocket
   - 检查跨域配置

3. **消息顺序问题**
   - 确保RabbitMQ配置中`prefetch=1`
   - 检查并发消费者设置为1

### 日志查看

```bash
# 查看应用日志
tail -f logs/digital-twin-websocket.log

# 查看RabbitMQ日志
tail -f /var/log/rabbitmq/rabbit@localhost.log
```

## 生产部署

### Docker部署

```dockerfile
FROM openjdk:11-jre-slim
COPY target/websocket-server-1.0.0.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 系统服务

创建systemd服务文件 `/etc/systemd/system/digital-twin-websocket.service`:

```ini
[Unit]
Description=Digital Twin WebSocket Server
After=network.target

[Service]
User=digital-twin
ExecStart=/usr/bin/java -jar /opt/digital-twin/websocket-server.jar
SuccessExitStatus=143
Restart=always

[Install]
WantedBy=multi-user.target
```

## 许可证

MIT License