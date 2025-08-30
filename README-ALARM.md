# 告警功能使用说明

## 功能概述

告警功能实现了从MQ消费传感器数据后，根据点位配置的告警规则进行实时分析，当数据触发告警条件时自动生成告警记录并存储到数据库。

## 告警规则配置

### 点位告警配置
在创建或更新点位时，可以配置以下告警参数：

- **alarmable**: 是否启用告警（true/false）
- **upperLimit**: 上限值（数值型点位）
- **upperHighLimit**: 上上限值（数值型点位）
- **lowerLimit**: 下限值（数值型点位）
- **lowerLowLimit**: 下下限值（数值型点位）
- **stateAlarm**: 状态告警值（布尔型点位）

### 告警类型

1. **上限告警**: 当数值 > upperLimit 时触发
2. **上上限告警**: 当数值 > upperHighLimit 时触发（优先级高于上限告警）
3. **下限告警**: 当数值 < lowerLimit 时触发
4. **下下限告警**: 当数值 < lowerLowLimit 时触发（优先级高于下限告警）
5. **状态告警**: 当布尔值等于 stateAlarm 配置值时触发

### 告警状态

1. **未确认**: 告警刚生成时的默认状态
2. **已确认**: 告警已被用户确认
3. **已忽略**: 告警已被用户忽略

## API接口

### 查询告警

#### 1. 根据设备ID查询告警
```bash
GET /api/alarms/device/{deviceId}
```

#### 2. 根据点位标识查询告警
```bash
GET /api/alarms/point/{pointId}
```

#### 3. 根据传感器ID查询告警
```bash
GET /api/alarms/sensor/{sensorId}
```

#### 4. 查询所有告警
```bash
GET /api/alarms/all
```

#### 5. 根据告警状态查询告警
```bash
GET /api/alarms/state/{state}
```

#### 6. 根据告警状态和设备ID查询告警
```bash
GET /api/alarms/state/{state}/device/{deviceId}
```

#### 7. 根据时间范围查询告警总次数
```bash
GET /api/alarms/count?timeRange={timeRange}
```
参数说明：
- timeRange: 时间范围，可选值为"今日"、"本周"、"本月"、"全年"

#### 8. 根据时间范围查询告警列表
```bash
GET /api/alarms/list?timeRange={timeRange}&pageNum={pageNum}&pageCount={pageCount}
```
参数说明：
- timeRange: 时间范围，可选值为"今日"、"本周"、"本月"、"全年"
- pageNum: 页码，从1开始
- pageCount: 每页数量

### 创建点位并配置告警

#### 数值型点位告警配置示例
```bash
curl -X POST http://localhost:8080/points \
  -H "Content-Type: application/json" \
  -d '{
    "identity": "temperature-sensor",
    "path": "ns=2;s=temperature",
    "writeable": false,
    "unit": "°C",
    "alarmable": true,
    "upperLimit": 30.0,
    "upperHighLimit": 35.0,
    "lowerLimit": 10.0,
    "lowerLowLimit": 5.0,
    "deviceId": 1
  }'
```

#### 布尔型点位告警配置示例
```bash
curl -X POST http://localhost:8080/points \
  -H "Content-Type: application/json" \
  -d '{
    "identity": "alarm-switch",
    "path": "ns=2;s=alarm_switch",
    "writeable": false,
    "unit": "",
    "alarmable": true,
    "stateAlarm": true,
    "deviceId": 1
  }'

### 告警操作接口

#### 1. 确认告警
```bash
curl -X POST http://localhost:8080/api/alarms/{alarmId}/acknowledge
```

#### 2. 忽略告警
```bash
curl -X POST http://localhost:8080/api/alarms/{alarmId}/ignore
```
```

## 测试告警功能

### 快速测试

1. 启动服务：
```bash
./test-local.sh
```

2. 运行告警测试脚本：
```bash
./test-alarm.sh
```

### 手动测试

1. 创建测试设备：
```bash
curl -X POST http://localhost:8080/devices \
  -H "Content-Type: application/json" \
  -d '{"name": "test-device", "description": "测试设备", "channelId": 1}'
```

2. 创建告警点位：
```bash
curl -X POST http://localhost:8080/points \
  -H "Content-Type: application/json" \
  -d '{
    "identity": "test-temp",
    "path": "ns=2;s=test_temp",
    "alarmable": true,
    "upperLimit": 25.0,
    "deviceId": 1
  }'
```

3. 发送测试数据触发告警：
```bash
curl -X POST http://localhost:8080/sensor/send \
  -H "Content-Type: application/json" \
  -d '{
    "ID": "test-sensor",
    "PointDataMap": {
      "test-temp": 30.0
    }
  }'
```

4. 查询告警结果：
```bash
curl http://localhost:8080/api/alarms/point/test-temp
```

## 数据结构

### 告警实体（Alarm）

- **id**: 告警ID（数据库自动生成）
- **timestamp**: 告警时间戳
- **sensorId**: 传感器ID
- **sensorTimestamp**: 传感器数据时间戳
- **pointId**: 点位标识
- **pointValue**: 点位值（字符串格式）
- **alarmType**: 告警类型
- **alarmThreshold**: 告警阈值
- **deviceId**: 设备ID
- **createdAt**: 创建时间
- **state**: 告警状态（未确认/已确认/已忽略）

## 性能优化

- 使用线程池异步处理每个点位的告警分析
- 每个点位数据独立处理，避免相互影响
- 支持高并发场景下的告警处理

## 注意事项

1. 告警分析是异步进行的，发送数据后需要等待几秒钟才能查询到告警结果
2. 字符串类型的点位值不会进行告警分析
3. 告警阈值比较时，数值类型统一转换为Double进行比较
4. 告警记录会永久保存，可以根据需要进行清理

## 日志查看

告警相关的日志可以在应用日志中查看：

```bash
tail -f logs/digital-twin-websocket.log | grep "告警"
```

日志示例：
```
INFO  c.d.a.s.AlarmAnalysisService - 开始分析传感器数据告警: test-sensor-001
INFO  c.d.a.s.AlarmAnalysisService - 生成告警: 设备=test-device, 点位=test-temp, 类型=上限告警, 值=30.0, 阈值=25.0
```