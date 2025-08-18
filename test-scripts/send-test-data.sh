#!/bin/bash

# 测试数据发送脚本
# 用于向RabbitMQ发送测试传感器数据

# RabbitMQ连接信息
RABBITMQ_HOST=${RABBITMQ_HOST:-localhost}
RABBITMQ_PORT=${RABBITMQ_PORT:-5672}
RABBITMQ_USER=${RABBITMQ_USER:-guest}
RABBITMQ_PASS=${RABBITMQ_PASS:-guest}

# 测试数据
SENSOR_DATA='{
  "sensorId": "TEMP-'$(date +%s)'",
  "timestamp": "'$(date -Iseconds)'",
  "value": '$(echo "scale=1; $RANDOM/1000" | bc)',
  "unit": "°C",
  "location": "Room-'$(($RANDOM % 10 + 1))'",
  "status": "normal"
}'

echo "发送测试数据到RabbitMQ..."
echo "数据: $SENSOR_DATA"

# 使用curl发送数据
curl -X POST "http://$RABBITMQ_HOST:15672/api/exchanges/%2f/sensor.data.exchange/publish" \
  -u "$RABBITMQ_USER:$RABBITMQ_PASS" \
  -H "content-type: application/json" \
  -d "{
    \"properties\":{},
    \"routing_key\":\"sensor.data\",
    \"payload\":\"$SENSOR_DATA\",
    \"payload_encoding\":\"string\"
  }"

echo "数据发送完成！"

# 如果安装了rabbitmqadmin，也可以使用这个命令
# rabbitmqadmin -H $RABBITMQ_HOST -u $RABBITMQ_USER -p $RABBITMQ_PASS publish \
#   routing_key="sensor.data" \
#   payload="$SENSOR_DATA"