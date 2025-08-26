package com.digitaltwin.websocket.controller;

import com.digitaltwin.websocket.annotation.PermAuth;
import com.digitaltwin.websocket.model.WebSocketResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 提供系统状态和健康检查接口
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    /**
     * 健康检查端点
     * 不需要权限，用于基本的系统状态检查
     * 
     * @return 健康状态响应
     */
    @GetMapping
    public WebSocketResponse<Map<String, Object>> health() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("service", "digital-twin-websocket-server");
        healthInfo.put("version", "1.0.0");
        
        return WebSocketResponse.success(healthInfo);
    }

    /**
     * 获取WebSocket连接信息
     * 需要数据查看权限
     * 
     * @return WebSocket连接状态
     */
    @GetMapping("/websocket")
    @PermAuth("data_view")
    public WebSocketResponse<Map<String, Object>> websocketInfo() {
        Map<String, Object> wsInfo = new HashMap<>();
        wsInfo.put("endpoint", "/ws");
        wsInfo.put("topic", "/topic/sensor-data");
        wsInfo.put("status", "enabled");
        
        return WebSocketResponse.success(wsInfo);
    }
}