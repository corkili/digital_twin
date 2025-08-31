# 告警设置接口使用说明

## 接口地址

```
POST /points/alarm
```

## 功能说明

该接口用于设置点位的告警配置，支持三种设置方式：

1. 所有点位设置：只传入点位的identity，设置所有相应点位的告警配置。
2. 唯一点位设置-1：传入点位的id，设置对应点位的告警配置。
3. 唯一点位设置-2：传入设备id和点位identity，设置对应点位的告警配置。

## 请求参数

| 参数名 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| id | Long | 否 | 点位ID，用于唯一点位设置-1 |
| deviceId | Long | 否 | 设备ID，用于唯一点位设置-2 |
| identity | String | 否 | 点位identity，用于所有点位设置和唯一点位设置-2 |
| alarmable | Boolean | 是 | 是否启用告警，只有启用告警时才需要设置其他的字段 |
| upperLimit | Double | 否 | 上限值（数值型点位） |
| upperHighLimit | Double | 否 | 上上限值（数值型点位） |
| lowerLimit | Double | 否 | 下限值（数值型点位） |
| lowerLowLimit | Double | 否 | 下下限值（数值型点位） |
| stateAlarm | Boolean | 否 | 状态告警值（布尔型点位） |

## 使用示例

### 1. 所有点位设置

```bash
curl -X POST http://localhost:8080/points/alarm \
  -H "Content-Type: application/json" \
  -d '{
    "identity": "temp",
    "alarmable": true,
    "upperLimit": 25.0,
    "upperHighLimit": 30.0,
    "lowerLimit": 10.0,
    "lowerLowLimit": 5.0
  }'
```

### 2. 唯一点位设置-1

```bash
curl -X POST http://localhost:8080/points/alarm \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "alarmable": true,
    "upperLimit": 25.0,
    "upperHighLimit": 30.0,
    "lowerLimit": 10.0,
    "lowerLowLimit": 5.0
  }'
```

### 3. 唯一点位设置-2

```bash
curl -X POST http://localhost:8080/points/alarm \
  -H "Content-Type: application/json" \
  -d '{
    "deviceId": 1,
    "identity": "temp",
    "alarmable": true,
    "upperLimit": 25.0,
    "upperHighLimit": 30.0,
    "lowerLimit": 10.0,
    "lowerLowLimit": 5.0
  }'
```

### 4. 禁用告警

```bash
curl -X POST http://localhost:8080/points/alarm \
  -H "Content-Type: application/json" \
  -d '{
    "id": 1,
    "alarmable": false
  }'
```

## 注意事项

1. 当 `alarmable` 为 `false` 或 `null` 时，只会更新 `alarmable` 字段，其他告警相关字段不会被更新。
2. 当 `alarmable` 为 `true` 时，会更新所有提供的告警相关字段。
3. 三种设置方式是互斥的，只能选择其中一种方式。
4. 如果请求参数不符合要求，接口会返回相应的错误信息。