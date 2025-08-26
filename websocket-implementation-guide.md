# WebSocket接口实现指南

## 📋 关键信息速览

### 🔗 WebSocket连接地址
**WebSocket服务器地址**: `http://umi.xyz:8081/api/ws`

**协议栈**:
- 底层协议: SockJS (提供WebSocket兼容层)
- 消息协议: STOMP (Simple Text Oriented Messaging Protocol)
- 实现方式可参考[代码](https://github.com/corkili/digital_twin/blob/master/web/src/services/WebSocketService.ts)

### 📡 订阅Topic
**主题路径**: `/topic/sensor-data`

### 📊 服务端推送的数据格式
```json
{
    "code": 200,
    "message": "success",
    "data": {
        "ID": "sensor-dbc531cf-96b6-414f-8469-10408573c99e",
        "HeatFlux": 116.08044752861235,
        "CoolingWater_In_Temp": 28.497404861320003,
        "Timestamp": 1755525788825
    }
}
```

**数据字段说明**:
- `code`: 响应状态码 (200表示成功)
- `message`: 响应消息
- `data.ID`: 传感器唯一标识符
- `data.HeatFlux`: 热通量值 (单位: W/m²)
- `data.CoolingWater_In_Temp`: 冷却水入口温度 (单位: °C)
- `data.Timestamp`: Unix时间戳 (毫秒)

---

## 2. 建立连接的方式

### 2.1 安装依赖
```bash
npm install sockjs-client @stomp/stompjs
```

### 2.2 创建连接实例
```typescript
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

class WebSocketConnection {
  private client: Client | null = null;
  private isConnected: boolean = false;

  constructor(private url: string = 'http://umi.xyz:8081/api/ws') {}

  connect() {
    // 防止重复连接
    if (this.client && this.client.active) {
      console.log('WebSocket已连接，无需重复连接');
      return;
    }

    // 创建STOMP客户端配置
    this.client = new Client({
      // SockJS工厂函数，创建底层连接
      webSocketFactory: () => new SockJS(this.url),
      
      // 调试日志
      debug: (str) => console.log('STOMP Debug:', str),
      
      // 自动重连配置
      reconnectDelay: 5000,  // 5秒后重连
      
      // 心跳配置
      heartbeatIncoming: 4000,  // 接收心跳间隔4秒
      heartbeatOutgoing: 4000,    // 发送心跳间隔4秒
    });

    // 连接成功回调
    this.client.onConnect = (frame) => {
      console.log('WebSocket连接成功:', frame);
      this.isConnected = true;
      
      // 连接成功后可以立即订阅主题
      this.subscribeToTopics();
    };

    // 连接断开回调
    this.client.onDisconnect = () => {
      console.log('WebSocket连接断开');
      this.isConnected = false;
    };

    // 错误处理回调
    this.client.onStompError = (frame) => {
      console.error('STOMP错误:', frame.headers['message']);
      console.error('错误详情:', frame.body);
    };

    // 激活连接
    this.client.activate();
  }

  disconnect() {
    if (this.client) {
      this.client.deactivate();
      this.client = null;
      this.isConnected = false;
    }
  }

  getConnectionStatus(): boolean {
    return this.isConnected;
  }
}
```

## 3. 数据订阅方式及Topic

### 3.1 订阅传感器数据
**Topic路径**: `/topic/sensor-data`

**订阅实现**:
```typescript
private subscribeToSensorData(callback: (data: any) => void) {
  if (!this.client || !this.client.connected) {
    console.error('WebSocket未连接，无法订阅');
    return;
  }

  // 订阅传感器数据主题
  const subscription = this.client.subscribe('/topic/sensor-data', (message) => {
    if (message.body) {
      try {
        // 解析JSON数据
        const response = JSON.parse(message.body);
        
        // 处理可能的嵌套数据格式
        const sensorData = response.data || response;
        
        // 调用回调函数处理数据
        callback(sensorData);
      } catch (error) {
        console.error('数据解析错误:', error);
      }
    }
  });

  // 返回订阅对象，可用于取消订阅
  return subscription;
}
```

### 3.2 数据格式定义
```typescript
interface SensorData {
  ID: string;                    // 传感器ID
  HeatFlux: number;              // 热通量值 (单位: W/m²)
  CoolingWater_In_Temp: number;  // 冷却水入口温度 (单位: °C)
  Timestamp: number;            // Unix时间戳 (毫秒)
}
```

### 3.3 完整订阅示例
```typescript
class SensorDataSubscriber {
  private client: Client;
  private sensorSubscription: any = null;

  subscribeSensorData(onDataReceived: (data: SensorData) => void) {
    // 确保连接已建立
    if (!this.client.connected) {
      console.error('等待WebSocket连接...');
      return;
    }

    // 订阅传感器数据
    this.sensorSubscription = this.client.subscribe('/topic/sensor-data', (message) => {
      try {
        const rawData = JSON.parse(message.body);
        const sensorData: SensorData = {
          ID: rawData.ID || rawData.id,
          HeatFlux: Number(rawData.HeatFlux || rawData.heatFlux),
          CoolingWater_In_Temp: Number(rawData.CoolingWater_In_Temp || rawData.coolingWaterInTemp),
          Timestamp: Number(rawData.Timestamp || rawData.timestamp)
        };
        
        onDataReceived(sensorData);
      } catch (error) {
        console.error('传感器数据处理错误:', error);
      }
    });

    console.log('已订阅传感器数据');
  }

  unsubscribeSensorData() {
    if (this.sensorSubscription) {
      this.sensorSubscription.unsubscribe();
      this.sensorSubscription = null;
      console.log('已取消传感器数据订阅');
    }
  }
}
```

## 4. 断开连接实现

### 4.1 优雅断开
```typescript
disconnectGracefully() {
  if (this.client) {
    // 取消所有订阅
    this.unsubscribeAll();
    
    // 断开连接
    this.client.deactivate();
    
    // 清理资源
    this.client = null;
    this.isConnected = false;
    
    console.log('WebSocket连接已优雅断开');
  }
}

private unsubscribeAll() {
  // 取消所有活跃的订阅
  if (this.sensorSubscription) {
    this.sensorSubscription.unsubscribe();
    this.sensorSubscription = null;
  }
}
```

## 5. 数据回调处理

### 5.1 数据回调管理
```typescript
class DataCallbackManager {
  private dataCallbacks: Array<(data: SensorData) => void> = [];
  private statusCallbacks: Array<(connected: boolean) => void> = [];

  // 注册数据回调
  addDataCallback(callback: (data: SensorData) => void) {
    this.dataCallbacks.push(callback);
  }

  // 移除数据回调
  removeDataCallback(callback: (data: SensorData) => void) {
    const index = this.dataCallbacks.indexOf(callback);
    if (index > -1) {
      this.dataCallbacks.splice(index, 1);
    }
  }

  // 触发所有数据回调
  triggerDataCallbacks(data: SensorData) {
    this.dataCallbacks.forEach(callback => {
      try {
        callback(data);
      } catch (error) {
        console.error('数据回调执行错误:', error);
      }
    });
  }

  // 注册状态回调
  addStatusCallback(callback: (connected: boolean) => void) {
    this.statusCallbacks.push(callback);
  }

  // 触发所有状态回调
  triggerStatusCallbacks(connected: boolean) {
    this.statusCallbacks.forEach(callback => {
      try {
        callback(connected);
      } catch (error) {
        console.error('状态回调执行错误:', error);
      }
    });
  }
}
```

## 6. 错误处理机制

### 6.1 连接错误处理
```typescript
private setupErrorHandlers() {
  // STOMP协议错误
  this.client.onStompError = (frame) => {
    console.error('STOMP协议错误:', {
      message: frame.headers['message'],
      body: frame.body,
      command: frame.command
    });
    
    // 触发错误回调
    this.onConnectionError?.({
      type: 'STOMP_ERROR',
      message: frame.headers['message']
    });
  };

  // WebSocket错误
  this.client.onWebSocketClose = (evt) => {
    console.error('WebSocket连接关闭:', {
      code: evt.code,
      reason: evt.reason,
      wasClean: evt.wasClean
    });
    
    if (!evt.wasClean) {
      // 非正常关闭，触发重连
      this.attemptReconnect();
    }
  };
}

private attemptReconnect() {
  if (this.reconnectAttempts < this.maxReconnectAttempts) {
    this.reconnectAttempts++;
    console.log(`尝试第${this.reconnectAttempts}次重连...`);
    
    setTimeout(() => {
      this.connect();
    }, this.reconnectDelay);
  } else {
    console.error('达到最大重连次数，停止重连');
  }
}
```

### 6.2 数据解析错误处理
```typescript
private safeParseMessage(message: Message): SensorData | null {
  try {
    const parsed = JSON.parse(message.body);
    
    // 数据验证
    if (!parsed.ID || typeof parsed.HeatFlux !== 'number') {
      throw new Error('无效的数据格式');
    }
    
    return {
      ID: parsed.ID,
      HeatFlux: parsed.HeatFlux,
      CoolingWater_In_Temp: parsed.CoolingWater_In_Temp,
      Timestamp: parsed.Timestamp
    };
  } catch (error) {
    console.error('消息解析失败:', error);
    return null;
  }
}
```

## 7. 完整使用示例

### 7.1 完整集成示例
```typescript
class WebSocketManager {
  private connection: WebSocketConnection;
  private callbackManager: DataCallbackManager;

  constructor() {
    this.connection = new WebSocketConnection();
    this.callbackManager = new DataCallbackManager();
    this.setupEventHandlers();
  }

  start() {
    this.connection.connect();
  }

  stop() {
    this.connection.disconnectGracefully();
  }

  onSensorData(callback: (data: SensorData) => void) {
    this.callbackManager.addDataCallback(callback);
  }

  onConnectionChange(callback: (connected: boolean) => void) {
    this.callbackManager.addStatusCallback(callback);
  }

  private setupEventHandlers() {
    // 连接成功后订阅数据
    this.connection.onConnect = () => {
      this.callbackManager.triggerStatusCallbacks(true);
      this.subscribeToSensorData();
    };

    this.connection.onDisconnect = () => {
      this.callbackManager.triggerStatusCallbacks(false);
    };
  }

  private subscribeToSensorData() {
    const subscription = this.connection.subscribe('/topic/sensor-data', (message) => {
      const data = this.safeParseMessage(message);
      if (data) {
        this.callbackManager.triggerDataCallbacks(data);
      }
    });
  }
}

// 使用示例
const wsManager = new WebSocketManager();

// 注册回调
wsManager.onSensorData((data) => {
  console.log('收到传感器数据:', data);
  // 更新UI
});

wsManager.onConnectionChange((connected) => {
  console.log('连接状态:', connected ? '已连接' : '已断开');
});

// 启动连接
wsManager.start();
```

## 8. 注意事项

### 8.1 连接管理
- **避免重复连接**: 在连接前检查`client.active`状态
- **资源清理**: 组件卸载时务必调用`disconnect()`
- **错误恢复**: 实现自动重连机制

### 8.2 性能优化
- **订阅管理**: 及时取消不需要的订阅
- **数据节流**: 对高频数据考虑节流处理
- **内存管理**: 及时移除不再使用的回调函数

### 8.3 调试建议
- **启用调试日志**: 设置`debug: (str) => console.log(str)`
- **连接监控**: 定期检查连接状态
- **错误日志**: 记录所有错误信息便于排查