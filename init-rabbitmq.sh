#!/bin/bash

# RabbitMQ初始化脚本
# 用于本地调试时初始化RabbitMQ所需的交换机和队列

echo "开始初始化RabbitMQ..."

# 检查RabbitMQ是否运行
if ! nc -z localhost 5672; then
    echo "错误：RabbitMQ服务未运行，请先启动："
    echo "  brew services start rabbitmq"
    exit 1
fi

# 创建交换机
echo "创建交换机..."
rabbitmqadmin -H localhost -u guest -p guest declare exchange name=sensor.data.exchange type=topic durable=true

# 创建队列
echo "创建队列..."
rabbitmqadmin -H localhost -u guest -p guest declare queue name=sensor.data.queue durable=true

# 绑定队列到交换机
echo "绑定队列到交换机..."
rabbitmqadmin -H localhost -u guest -p guest declare binding source=sensor.data.exchange destination=sensor.data.queue routing_key=sensor.data

# 设置权限（如果需要）
echo "设置权限..."
rabbitmqctl set_permissions -p / guest ".*" ".*" ".*"

echo "RabbitMQ初始化完成！"
echo ""
echo "可以访问 http://localhost:15672 查看管理界面"
echo "用户名/密码：guest/guest"