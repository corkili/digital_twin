package com.digitaltwin.websocket.service;

import com.digitaltwin.websocket.model.WebSocketResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * WebSocket推送服务
 * 负责将消息推送到已连接的WebSocket客户端
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketPushService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 推送消息到所有订阅了传感器数据主题的客户端
     * 
     * @param response 要推送的响应数据
     * @param <T> 响应数据的类型
     */
    public <T> void pushToSubscribers(WebSocketResponse<T> response) {
        try {
            // 检查是否有活跃的WebSocket会话
            // Spring会自动管理连接状态，未建立连接时不会推送
            
            messagingTemplate.convertAndSend("/topic/sensor-data", response);
            log.debug("消息已推送到WebSocket主题: /topic/sensor-data");
            
        } catch (Exception e) {
            log.error("推送消息到WebSocket时发生错误: {}", e.getMessage(), e);
        }
    }

    /**
     * 推送告警消息到所有订阅了告警数据主题的客户端
     * 
     * @param response 要推送的响应数据
     * @param <T> 响应数据的类型
     */
    public <T> void pushAlarmToSubscribers(WebSocketResponse<T> response) {
        try {
            messagingTemplate.convertAndSend("/topic/alarm-data", response);
            log.debug("告警消息已推送到WebSocket主题: /topic/alarm-data");
            
        } catch (Exception e) {
            log.error("推送告警消息到WebSocket时发生错误: {}", e.getMessage(), e);
        }
    }

    /**
     * 推送消息到特定用户
     * 
     * @param username 目标用户名
     * @param response 要推送的响应数据
     * @param <T> 响应数据的类型
     */
    public <T> void pushToUser(String username, WebSocketResponse<T> response) {
        try {
            messagingTemplate.convertAndSendToUser(username, "/queue/sensor-data", response);
            log.debug("消息已推送到用户: {}", username);
            
        } catch (Exception e) {
            log.error("推送消息到用户 {} 时发生错误: {}", username, e.getMessage(), e);
        }
    }
}