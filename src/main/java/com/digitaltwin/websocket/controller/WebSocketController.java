package com.digitaltwin.websocket.controller;

import com.digitaltwin.websocket.annotation.PermAuth;
import com.digitaltwin.websocket.model.WebSocketResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/**
 * WebSocket控制器
 * 处理WebSocket消息和连接
 */
@Slf4j
@Controller
public class WebSocketController {

    /**
     * 处理客户端订阅传感器数据的请求
     * 需要数据查看权限
     * 
     * @param message 客户端发送的消息
     * @return 响应消息
     */
    @MessageMapping("/sensor-data/subscribe")
    @SendTo("/topic/sensor-data")
    @PermAuth("data_view")
    public WebSocketResponse<String> handleSensorDataSubscription(String message) {
        log.info("客户端订阅传感器数据: {}", message);
        return WebSocketResponse.success("已订阅传感器数据流");
    }

    /**
     * 处理心跳消息
     * 
     * @param message 心跳消息
     * @return 心跳响应
     */
    @MessageMapping("/heartbeat")
    @SendTo("/user/queue/heartbeat")
    public WebSocketResponse<String> handleHeartbeat(String message) {
        log.debug("收到心跳消息: {}", message);
        return WebSocketResponse.success("pong");
    }
}