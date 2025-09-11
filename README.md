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

## 仿真试验打分机制

### 打分规则

仿真试验系统包含自动打分功能，当用户提交试验步骤数组时会自动计算得分：

#### 打分逻辑

1. **比对对象**：
   - 标准答案：存储在 `simulation_experiment` 表的 `steps_data` 字段中的**步骤数组**
   - 用户答案：通过 `/simulations/submit` 接口提交的试验步骤数组 `experimentSteps`

2. **比对单位**：
   - 从所有步骤中提取所有 `SimulationStepNode` 的 `name` 字段进行比对
   - 只比对第一层节点，不包括嵌套的 `child` 节点

3. **比对方式**：
   - 按照步骤(Step) → 角色(Role) → 任务(Task)的顺序提取所有节点名称
   - 严格按顺序进行一对一比对
   - 名称完全匹配才算正确

4. **分数计算**：
   - 总分：100分
   - 单节点分值：`100 ÷ 总节点数量`（向下取整）
   - 最终得分：`正确节点数量 × 单节点分值`
   - 结果为整数（自动抹掉小数）

#### 打分示例

**标准答案（步骤数组）：**
```json
[
  {
    "stepId": 1,
    "stepName": "传感器初始化",
    "roles": [
      {
        "roleId": "role1",
        "roleName": "操作员",
        "tasks": [
          {"name": "打开电源开关"},
          {"name": "检查传感器状态"}
        ]
      }
    ]
  },
  {
    "stepId": 2,
    "stepName": "数据采集",
    "roles": [
      {
        "roleId": "role1",
        "roleName": "操作员",
        "tasks": [
          {"name": "启动采集程序"},
          {"name": "设置采集频率"}
        ]
      },
      {
        "roleId": "role2",
        "roleName": "监控员",
        "tasks": [
          {"name": "监控数据质量"}
        ]
      }
    ]
  }
]
```

**用户答案（步骤数组）：**
```json
[
  {
    "stepId": 1,
    "stepName": "传感器初始化",
    "roles": [
      {
        "roleId": "role1",
        "roleName": "操作员",
        "tasks": [
          {"name": "打开电源开关"},      // ✓ 正确
          {"name": "检查设备状态"}       // ✗ 错误
        ]
      }
    ]
  },
  {
    "stepId": 2,
    "stepName": "数据采集",
    "roles": [
      {
        "roleId": "role1",
        "roleName": "操作员",
        "tasks": [
          {"name": "启动采集程序"},      // ✓ 正确
          {"name": "设置采集频率"}       // ✓ 正确
        ]
      },
      {
        "roleId": "role2",
        "roleName": "监控员",
        "tasks": [
          {"name": "监控数据质量"}       // ✓ 正确
        ]
      }
    ]
  }
]
```

**计算过程：**
- 按顺序提取的节点名称：
  1. "打开电源开关" ✓
  2. "检查传感器状态" vs "检查设备状态" ✗
  3. "启动采集程序" ✓
  4. "设置采集频率" ✓
  5. "监控数据质量" ✓
- 总节点数：5个
- 单节点分值：100 ÷ 5 = 20分
- 正确节点：4个（第2个错误）
- 最终得分：4 × 20 = 80分

#### API接口变化

**GET `/simulations/{id}/steps`** - 获取试验步骤
- 返回：`ExperimentStepsDto`，包含步骤数组和元信息

**POST `/simulations/submit`** - 提交试验步骤
- 请求体字段：`experimentSteps`（数组类型）
- 示例：
```json
{
  "userId": 123,
  "auapUserId": "user001",
  "targetExperimentId": 456,
  "experimentSteps": [
    // 步骤数组
  ]
}
```

#### 数据存储

- 计算出的分数存储在 `simulation_user_experiment` 表的 `score` 字段中
- 分数范围：0-100的整数
- 如果计算失败或找不到标准答案，默认得分为0

#### 示例数据库记录

项目提供了示例SQL脚本来创建测试数据：

**执行脚本：**
```bash
# 使用简化版本（推荐）
mysql -u username -p database_name < sample_experiment_steps_simple.sql

# 或使用MySQL JSON函数版本
mysql -u username -p database_name < sample_experiment_steps.sql
```

**示例数据包含：**

1. **传感器数据采集实验** - 3个步骤，4个角色，13个任务节点
   - 步骤1: 传感器初始化（操作员、监督员）
   - 步骤2: 数据采集配置（操作员）
   - 步骤3: 实时监控（监控员、分析员）

2. **工业设备维护实验** - 3个步骤，4个角色，11个任务节点
   - 步骤1: 设备停机准备（安全员）
   - 步骤2: 维护检查（维护技术员、质检员）
   - 步骤3: 设备重启（操作员）

**验证数据：**
```sql
-- 查看插入的试验数据
SELECT id, name, description, status, created_at 
FROM simulation_experiment 
WHERE name LIKE '%实验';

-- 查看具体的步骤数据（格式化显示）
SELECT name, JSON_PRETTY(steps_data) AS formatted_steps 
FROM simulation_experiment 
WHERE id = 1;
```

**数据结构说明：**
- `steps_data` 字段存储JSON格式的步骤数组
- 每个步骤包含：stepId, stepName, roles
- 每个角色包含：roleId, roleName, tasks  
- 每个任务包含：name（用于打分比对的关键字段）

## 许可证

MIT License