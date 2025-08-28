#!/bin/bash

# 告警功能测试脚本

echo "=== 告警功能测试脚本 ==="
echo

# 检查Java进程是否在运行
if pgrep -f "DigitalTwinWebsocketApplication" > /dev/null; then
    echo "✅ 数字孪生服务正在运行"
else
    echo "❌ 数字孪生服务未运行，请先启动服务"
    echo "使用命令: ./test-local.sh"
    exit 1
fi

# 等待服务完全启动
echo "等待服务完全启动..."
sleep 5

# 创建测试设备
echo "创建测试设备..."
curl -s -X POST http://localhost:8080/devices \
  -H "Content-Type: application/json" \
  -d '{
    "name": "test-device-001",
    "description": "测试设备用于告警功能",
    "channelId": 1
  }' > /dev/null

# 获取设备ID
DEVICE_ID=$(curl -s http://localhost:8080/devices/name/test-device-001 | jq -r '.data.id')
echo "测试设备ID: $DEVICE_ID"

# 创建测试点位 - 数值型告警
echo "创建数值型告警点位..."
curl -s -X POST http://localhost:8080/points \
  -H "Content-Type: application/json" \
  -d '{
    "identity": "temperature-sensor-001",
    "path": "ns=2;s=temperature",
    "writeable": false,
    "unit": "°C",
    "alarmable": true,
    "upperLimit": 30.0,
    "upperHighLimit": 35.0,
    "lowerLimit": 10.0,
    "lowerLowLimit": 5.0,
    "deviceId": '$DEVICE_ID'
  }' > /dev/null

# 创建测试点位 - 布尔型告警
echo "创建布尔型告警点位..."
curl -s -X POST http://localhost:8080/points \
  -H "Content-Type: application/json" \
  -d '{
    "identity": "alarm-switch-001",
    "path": "ns=2;s=alarm_switch",
    "writeable": false,
    "unit": "",
    "alarmable": true,
    "stateAlarm": true,
    "deviceId": '$DEVICE_ID'
  }' > /dev/null

echo "测试点位创建完成"
echo

# 发送测试数据 - 触发上限告警
echo "发送测试数据 - 触发上限告警..."
curl -s -X POST http://localhost:8080/sensor/send \
  -H "Content-Type: application/json" \
  -d '{
    "ID": "test-sensor-001",
    "Timestamp": '$(date +%s)000',
    "PointDataMap": {
      "temperature-sensor-001": 32.5,
      "alarm-switch-001": false
    }
  }' > /dev/null

echo "等待告警处理..."
sleep 3

# 发送测试数据 - 触发上上限告警
echo "发送测试数据 - 触发上上限告警..."
curl -s -X POST http://localhost:8080/sensor/send \
  -H "Content-Type: application/json" \
  -d '{
    "ID": "test-sensor-002",
    "Timestamp": '$(date +%s)000',
    "PointDataMap": {
      "temperature-sensor-001": 36.0,
      "alarm-switch-001": false
    }
  }' > /dev/null

echo "等待告警处理..."
sleep 3

# 发送测试数据 - 触发状态告警
echo "发送测试数据 - 触发状态告警..."
curl -s -X POST http://localhost:8080/sensor/send \
  -H "Content-Type: application/json" \
  -d '{
    "ID": "test-sensor-003",
    "Timestamp": '$(date +%s)000',
    "PointDataMap": {
      "temperature-sensor-001": 25.0,
      "alarm-switch-001": true
    }
  }' > /dev/null

echo "等待告警处理..."
sleep 3

# 查询告警结果
echo
echo "=== 查询告警结果 ==="
echo "设备告警:"
curl -s http://localhost:8080/api/alarms/device/$DEVICE_ID | jq '.data[] | {id: .id, pointId: .pointId, alarmType: .alarmType, pointValue: .pointValue, alarmThreshold: .alarmThreshold}'

echo
echo "点位告警:"
curl -s http://localhost:8080/api/alarms/point/temperature-sensor-001 | jq '.data[] | {id: .id, alarmType: .alarmType, pointValue: .pointValue, alarmThreshold: .alarmThreshold}'

echo
echo "所有告警:"
curl -s http://localhost:8080/api/alarms/all | jq '.data[] | {id: .id, pointId: .pointId, alarmType: .alarmType, pointValue: .pointValue}'

echo
echo "=== 告警功能测试完成 ==="