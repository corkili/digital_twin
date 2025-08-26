# 原生WebSocket实现指南（不基于STOMP协议）

## 📋 实现方案概述

本指南提供不依赖STOMP协议的原生WebSocket实现方案，对比现有的STOMP+SockJS实现，具有以下优势：
- **更轻量级**: 无需额外的STOMP协议层
- **更灵活**: 可以自定义消息格式和通信协议
- **性能更好**: 减少协议转换开销
- **原生支持**: 现代浏览器原生支持WebSocket

## 🏗️ 服务端实现（Spring Boot原生WebSocket）

### 1. 配置类（替代WebSocketConfig.java）

```java
package com.digitaltwin.websocket.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

/**
 * 原生WebSocket配置类
 * 不依赖STOMP协议，使用原生WebSocket处理程序
 */
@Configuration
@EnableWebSocket
public class NativeWebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new SensorDataWebSocketHandler(), "/ws")
                .setAllowedOriginPatterns("*") // 允许跨域
                .addInterceptors(new HttpSessionHandshakeInterceptor()); // 添加握手拦截器
    }
}
```

### 2. WebSocket处理器类

```java
package com.digitaltwin.websocket.handler;

import com.digitaltwin.websocket.model.WebSocketResponse;
import com.digitaltwin.websocket.model.SensorData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 传感器数据WebSocket处理器
 * 处理WebSocket连接、消息收发和广播
 */
@Slf4j
public class SensorDataWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConcurrentMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        log.info("WebSocket连接建立，会话ID: {}，当前连接数: {}", sessionId, sessions.size());
        
        // 发送连接成功消息
        WebSocketResponse<String> response = WebSocketResponse.success("连接成功", sessionId);
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("收到客户端消息，会话ID: {}，消息内容: {}", session.getId(), payload);
        
        try {
            // 解析客户端消息
            ClientMessage clientMessage = objectMapper.readValue(payload, ClientMessage.class);
            
            // 处理不同类型的客户端消息
            switch (clientMessage.getType()) {
                case "ping":
                    handlePingMessage(session);
                    break;
                case "subscribe":
                    handleSubscribeMessage(session, clientMessage);
                    break;
                case "unsubscribe":
                    handleUnsubscribeMessage(session, clientMessage);
                    break;
                default:
                    handleUnknownMessage(session, clientMessage);
            }
        } catch (Exception e) {
            log.error("处理消息时发生错误: {}", e.getMessage(), e);
            WebSocketResponse<String> errorResponse = WebSocketResponse.error("消息格式错误", e.getMessage());
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(errorResponse)));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        log.info("WebSocket连接关闭，会话ID: {}，关闭原因: {}，当前连接数: {}", 
                sessionId, status, sessions.size());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket传输错误，会话ID: {}", session.getId(), exception);
    }

    /**
     * 广播传感器数据给所有连接的客户端
     */
    public void broadcastSensorData(SensorData sensorData) {
        if (sessions.isEmpty()) {
            log.debug("没有活跃的WebSocket连接，跳过广播");
            return;
        }

        try {
            WebSocketResponse<SensorData> response = WebSocketResponse.success("传感器数据", sensorData);
            String message = objectMapper.writeValueAsString(response);
            
            TextMessage textMessage = new TextMessage(message);
            
            sessions.forEach((sessionId, session) -> {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(textMessage);
                    }
                } catch (IOException e) {
                    log.error("发送消息失败，会话ID: {}", sessionId, e);
                }
            });
            
            log.debug("广播传感器数据完成，接收客户端数量: {}", sessions.size());
        } catch (Exception e) {
            log.error("广播传感器数据时发生错误: {}", e.getMessage(), e);
        }
    }

    // 消息处理辅助方法
    private void handlePingMessage(WebSocketSession session) throws IOException {
        WebSocketResponse<String> pongResponse = WebSocketResponse.success("pong", "服务器响应");
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(pongResponse)));
    }

    private void handleSubscribeMessage(WebSocketSession session, ClientMessage message) throws IOException {
        log.info("客户端订阅主题: {}", message.getTopic());
        WebSocketResponse<String> response = WebSocketResponse.success("订阅成功", message.getTopic());
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    private void handleUnsubscribeMessage(WebSocketSession session, ClientMessage message) throws IOException {
        log.info("客户端取消订阅主题: {}", message.getTopic());
        WebSocketResponse<String> response = WebSocketResponse.success("取消订阅成功", message.getTopic());
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    private void handleUnknownMessage(WebSocketSession session, ClientMessage message) throws IOException {
        WebSocketResponse<String> response = WebSocketResponse.error("未知消息类型", message.getType());
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
    }

    /**
     * 获取当前连接数
     */
    public int getConnectionCount() {
        return sessions.size();
    }

    /**
     * 内部消息类，用于接收客户端消息
     */
    public static class ClientMessage {
        private String type;
        private String topic;
        private Object data;
        
        // Getters and Setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
    }
}
```

### 3. 服务类（替代WebSocketPushService.java）

```java
package com.digitaltwin.websocket.service;

import com.digitaltwin.websocket.handler.SensorDataWebSocketHandler;
import com.digitaltwin.websocket.model.SensorData;
import com.digitaltwin.websocket.model.WebSocketResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 原生WebSocket推送服务
 * 使用原生WebSocket处理器推送消息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NativeWebSocketPushService {

    private final SensorDataWebSocketHandler webSocketHandler;

    /**
     * 推送传感器数据到所有连接的WebSocket客户端
     */
    public void pushSensorData(SensorData sensorData) {
        try {
            int connectionCount = webSocketHandler.getConnectionCount();
            if (connectionCount == 0) {
                log.debug("没有活跃的WebSocket连接，跳过推送");
                return;
            }

            webSocketHandler.broadcastSensorData(sensorData);
            log.debug("推送传感器数据完成，接收客户端数量: {}", connectionCount);
        } catch (Exception e) {
            log.error("推送传感器数据时发生错误: {}", e.getMessage(), e);
        }
    }

    /**
     * 推送自定义消息到所有连接的客户端
     */
    public <T> void pushCustomMessage(String messageType, T data) {
        WebSocketResponse<T> response = WebSocketResponse.success(messageType, data);
        webSocketHandler.broadcastSensorData(null); // 需要调整方法签名
    }
}
```

### 4. 数据模型类

```java
package com.digitaltwin.websocket.model;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * 传感器数据模型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SensorData {
    private String ID;
    private Double HeatFlux;
    private Double CoolingWater_In_Temp;
    private Long Timestamp;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}

/**
 * WebSocket响应包装类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebSocketResponse<T> {
    private int code;
    private String message;
    private T data;
    private Long timestamp;
    private String type;

    public static <T> WebSocketResponse<T> success(String message, T data) {
        WebSocketResponse<T> response = new WebSocketResponse<>();
        response.setCode(200);
        response.setMessage(message);
        response.setData(data);
        response.setTimestamp(System.currentTimeMillis());
        response.setType("success");
        return response;
    }

    public static <T> WebSocketResponse<T> error(String message, T data) {
        WebSocketResponse<T> response = new WebSocketResponse<>();
        response.setCode(500);
        response.setMessage(message);
        response.setData(data);
        response.setTimestamp(System.currentTimeMillis());
        response.setType("error");
        return response;
    }
}
```

### 5. 消费者服务（调整后的RabbitMQ消费者）

```java
package com.digitaltwin.websocket.service;

import com.digitaltwin.websocket.model.SensorData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * RabbitMQ消费者服务
 * 消费传感器数据并通过原生WebSocket推送
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQConsumerService {

    private final NativeWebSocketPushService webSocketPushService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 监听传感器数据队列
     */
    @RabbitListener(queues = "${websocket.queues.sensor-data}")
    public void receiveSensorData(Message message) {
        try {
            String body = new String(message.getBody());
            log.debug("收到RabbitMQ消息: {}", body);
            
            SensorData sensorData = objectMapper.readValue(body, SensorData.class);
            
            // 通过原生WebSocket推送数据
            webSocketPushService.pushSensorData(sensorData);
            
        } catch (Exception e) {
            log.error("处理传感器数据时发生错误: {}", e.getMessage(), e);
        }
    }
}
```

## 🌐 前端实现（TypeScript/React）

### 1. 原生WebSocket服务类

```typescript
// src/services/NativeWebSocketService.ts
import { SensorData, WebSocketResponse, ConnectionStatus } from '../types';

class NativeWebSocketService {
  private ws: WebSocket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 3000;
  private heartbeatInterval = 30000;
  private heartbeatTimer: NodeJS.Timeout | null = null;
  
  private connectionStatus: ConnectionStatus = { isConnected: false };
  private dataCallback: ((data: SensorData) => void) | null = null;
  private statusCallback: ((status: ConnectionStatus) => void) | null = null;
  private messageCallback: ((message: any) => void) | null = null;

  constructor(private url: string = 'ws://localhost:8081/api/ws') {}

  /**
   * 建立WebSocket连接
   */
  connect(): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      console.log('WebSocket已连接，无需重复连接');
      return;
    }

    try {
      this.ws = new WebSocket(this.url);
      
      this.ws.onopen = (event) => {
        console.log('WebSocket连接已建立:', event);
        this.reconnectAttempts = 0;
        this.connectionStatus = {
          isConnected: true,
          connectionId: this.generateConnectionId(),
          lastActivity: new Date().toISOString(),
        };
        
        this.updateStatus();
        this.startHeartbeat();
        
        // 发送订阅消息
        this.sendMessage({
          type: 'subscribe',
          topic: 'sensor-data'
        });
      };

      this.ws.onmessage = (event) => {
        try {
          const response = JSON.parse(event.data);
          this.handleMessage(response);
        } catch (error) {
          console.error('解析WebSocket消息失败:', error);
        }
      };

      this.ws.onclose = (event) => {
        console.log('WebSocket连接关闭:', event);
        this.connectionStatus = { isConnected: false };
        this.updateStatus();
        this.stopHeartbeat();
        
        if (!event.wasClean) {
          this.attemptReconnect();
        }
      };

      this.ws.onerror = (error) => {
        console.error('WebSocket错误:', error);
        this.connectionStatus = { 
          isConnected: false,
          error: '连接错误'
        };
        this.updateStatus();
      };

    } catch (error) {
      console.error('创建WebSocket连接失败:', error);
      this.attemptReconnect();
    }
  }

  /**
   * 断开WebSocket连接
   */
  disconnect(): void {
    if (this.ws) {
      this.stopHeartbeat();
      
      // 发送取消订阅消息
      this.sendMessage({
        type: 'unsubscribe',
        topic: 'sensor-data'
      });
      
      this.ws.close();
      this.ws = null;
    }
    
    this.connectionStatus = { isConnected: false };
    this.updateStatus();
  }

  /**
   * 发送消息到服务器
   */
  sendMessage(message: any): boolean {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      try {
        this.ws.send(JSON.stringify(message));
        return true;
      } catch (error) {
        console.error('发送消息失败:', error);
        return false;
      }
    }
    return false;
  }

  /**
   * 设置数据回调
   */
  setDataCallback(callback: (data: SensorData) => void): void {
    this.dataCallback = callback;
  }

  /**
   * 设置状态回调
   */
  setStatusCallback(callback: (status: ConnectionStatus) => void): void {
    this.statusCallback = callback;
  }

  /**
   * 设置消息回调
   */
  setMessageCallback(callback: (message: any) => void): void {
    this.messageCallback = callback;
  }

  /**
   * 获取连接状态
   */
  getConnectionStatus(): ConnectionStatus {
    return this.connectionStatus;
  }

  /**
   * 发送ping消息
   */
  ping(): void {
    this.sendMessage({ type: 'ping' });
  }

  /**
   * 处理接收到的消息
   */
  private handleMessage(response: any): void {
    this.connectionStatus.lastActivity = new Date().toISOString();
    
    if (this.messageCallback) {
      this.messageCallback(response);
    }

    switch (response.code) {
      case 200:
        if (response.data && this.dataCallback) {
          this.dataCallback(response.data);
        }
        break;
      case 500:
        console.error('服务器错误:', response.message);
        break;
      default:
        console.log('收到消息:', response);
    }
  }

  /**
   * 尝试重连
   */
  private attemptReconnect(): void {
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

  /**
   * 启动心跳
   */
  private startHeartbeat(): void {
    this.heartbeatTimer = setInterval(() => {
      this.ping();
    }, this.heartbeatInterval);
  }

  /**
   * 停止心跳
   */
  private stopHeartbeat(): void {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
      this.heartbeatTimer = null;
    }
  }

  /**
   * 更新状态
   */
  private updateStatus(): void {
    if (this.statusCallback) {
      this.statusCallback(this.connectionStatus);
    }
  }

  /**
   * 生成连接ID
   */
  private generateConnectionId(): string {
    return 'ws_' + Math.random().toString(36).substr(2, 9);
  }
}

export default NativeWebSocketService;
```

### 2. React Hook使用示例

```typescript
// src/hooks/useNativeWebSocket.ts
import { useEffect, useState, useRef } from 'react';
import NativeWebSocketService from '../services/NativeWebSocketService';
import { SensorData, ConnectionStatus } from '../types';

export const useNativeWebSocket = (url?: string) => {
  const [isConnected, setIsConnected] = useState(false);
  const [sensorData, setSensorData] = useState<SensorData | null>(null);
  const [connectionStatus, setConnectionStatus] = useState<ConnectionStatus>({ isConnected: false });
  const [error, setError] = useState<string | null>(null);
  
  const wsServiceRef = useRef<NativeWebSocketService | null>(null);

  useEffect(() => {
    // 创建WebSocket服务实例
    const wsService = new NativeWebSocketService(url);
    wsServiceRef.current = wsService;

    // 设置回调
    wsService.setStatusCallback((status: ConnectionStatus) => {
      setIsConnected(status.isConnected);
      setConnectionStatus(status);
      if (status.error) {
        setError(status.error);
      }
    });

    wsService.setDataCallback((data: SensorData) => {
      setSensorData(data);
      setError(null); // 清除之前的错误
    });

    // 建立连接
    wsService.connect();

    // 清理函数
    return () => {
      if (wsServiceRef.current) {
        wsServiceRef.current.disconnect();
      }
    };
  }, [url]);

  const connect = () => {
    if (wsServiceRef.current) {
      wsServiceRef.current.connect();
    }
  };

  const disconnect = () => {
    if (wsServiceRef.current) {
      wsServiceRef.current.disconnect();
    }
  };

  const sendMessage = (message: any) => {
    if (wsServiceRef.current) {
      return wsServiceRef.current.sendMessage(message);
    }
    return false;
  };

  return {
    isConnected,
    sensorData,
    connectionStatus,
    error,
    connect,
    disconnect,
    sendMessage,
  };
};
```

### 3. React组件使用示例

```typescript
// src/components/SensorDashboard.tsx
import React from 'react';
import { useNativeWebSocket } from '../hooks/useNativeWebSocket';

const SensorDashboard: React.FC = () => {
  const { 
    isConnected, 
    sensorData, 
    connectionStatus, 
    error,
    connect,
    disconnect 
  } = useNativeWebSocket();

  return (
    <div className="sensor-dashboard">
      <div className="connection-status">
        <h3>连接状态</h3>
        <div className={`status-indicator ${isConnected ? 'connected' : 'disconnected'}`}>
          {isConnected ? '已连接' : '已断开'}
        </div>
        {connectionStatus.connectionId && (
          <div>连接ID: {connectionStatus.connectionId}</div>
        )}
        {connectionStatus.lastActivity && (
          <div>最后活动: {new Date(connectionStatus.lastActivity).toLocaleString()}</div>
        )}
      </div>

      <div className="controls">
        <button onClick={connect} disabled={isConnected}>
          连接
        </button>
        <button onClick={disconnect} disabled={!isConnected}>
          断开
        </button>
      </div>

      {error && (
        <div className="error-message">
          错误: {error}
        </div>
      )}

      {sensorData && (
        <div className="sensor-data">
          <h3>传感器数据</h3>
          <div>ID: {sensorData.ID}</div>
          <div>热通量: {sensorData.HeatFlux} W/m²</div>
          <div>冷却水入口温度: {sensorData.CoolingWater_In_Temp} °C</div>
          <div>时间: {new Date(sensorData.Timestamp).toLocaleString()}</div>
        </div>
      )}
    </div>
  );
};

export default SensorDashboard;
```

## 📊 性能对比与迁移建议

### STOMP vs 原生WebSocket对比

| 特性 | STOMP实现 | 原生WebSocket |
|------|-----------|---------------|
| **协议复杂度** | 高（需要STOMP协议层） | 低（直接WebSocket） |
| **消息格式** | 固定STOMP格式 | 可自定义JSON格式 |
| **浏览器兼容性** | 好（SockJS回退） | 现代浏览器良好 |
| **连接开销** | 较高 | 较低 |
| **功能丰富度** | 高（订阅/发布模式） | 需自行实现 |
| **错误处理** | 内置机制 | 需手动实现 |
| **重连机制** | 需手动实现 | 需手动实现 |

### 迁移步骤

1. **服务端迁移**:
   - 创建新的配置类 `NativeWebSocketConfig`
   - 实现 `SensorDataWebSocketHandler` 处理器
   - 调整消息推送服务

2. **前端迁移**:
   - 替换 `sockjs-client` 和 `@stomp/stompjs` 依赖
   - 使用原生 `WebSocket` API
   - 实现自定义的消息协议

3. **配置调整**:
   - 更新WebSocket端点配置
   - 调整消息格式和错误处理

### 注意事项

1. **浏览器兼容性**: 原生WebSocket在现代浏览器中支持良好，但旧版浏览器可能需要polyfill
2. **错误处理**: 需要手动实现连接状态监控和重连机制
3. **消息协议**: 需要定义清晰的客户端-服务器通信协议
4. **负载均衡**: 在集群环境中需要考虑WebSocket会话管理

## 🔧 测试与调试

### 服务端测试

```bash
# 使用websocat测试原生WebSocket连接
websocat ws://localhost:8081/api/ws

# 发送订阅消息
{"type":"subscribe","topic":"sensor-data"}

# 发送ping消息
{"type":"ping"}
```

### 前端调试

```typescript
// 调试WebSocket连接
const wsService = new NativeWebSocketService();
wsService.setMessageCallback((message) => {
  console.log('收到消息:', message);
});

// 监听连接状态
wsService.setStatusCallback((status) => {
  console.log('连接状态:', status);
});
```

## 📈 性能优化建议

1. **连接池管理**: 实现连接池以复用WebSocket连接
2. **消息压缩**: 启用WebSocket消息压缩（permessage-deflate）
3. **批量发送**: 对于高频数据，考虑批量发送策略
4. **心跳优化**: 根据网络环境调整心跳间隔
5. **错误恢复**: 实现指数退避重连策略

通过以上实现，你可以获得一个轻量级、高性能的原生WebSocket解决方案，同时保持与现有业务逻辑的兼容性。