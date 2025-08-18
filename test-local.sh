#!/bin/bash

# 本地调试测试脚本
# 用于测试WebSocket连接和RabbitMQ消息传递

echo "=== 数字孪生项目本地调试测试 ==="
echo ""

# 检查应用是否运行
if curl -s http://localhost:8081/api/actuator/health > /dev/null; then
    echo "✅ WebSocket服务器正在运行"
else
    echo "❌ WebSocket服务器未启动"
    exit 1
fi

# 检查RabbitMQ管理界面
echo ""
echo "📊 RabbitMQ管理界面: http://localhost:15672"
echo "   用户名/密码: guest/guest"
echo ""

# 检查WebSocket端点
echo "🔗 WebSocket连接端点:"
echo "   ws://localhost:8081/api/ws"
echo ""

# 显示可用主题
echo "📡 可用WebSocket主题:"
echo "   /topic/sensor-data - 传感器数据"
echo "   /topic/heartbeat - 心跳数据"
echo ""

# 显示RabbitMQ队列
echo "📮 RabbitMQ队列状态:"
rabbitmqadmin -H localhost -u guest -p guest list queues name messages consumers

echo ""
echo "=== 测试命令示例 ==="
echo "1. 使用websocat测试WebSocket连接:"
echo "   websocat ws://localhost:8081/api/ws"
echo ""
echo "2. 发送测试消息到RabbitMQ:"
echo "   rabbitmqadmin -H localhost -u guest -p guest publish routing_key=sensor.data payload='{\"temperature\":25.5,\"humidity\":60}'"
echo ""
echo "3. 查看日志:"
echo "   tail -f logs/digital-twin-websocket.log"