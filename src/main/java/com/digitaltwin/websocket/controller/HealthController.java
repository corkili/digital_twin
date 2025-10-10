package com.digitaltwin.websocket.controller;

import com.digitaltwin.websocket.model.WebSocketResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "系统健康检查", description = "提供系统健康状态检查和WebSocket连接信息查询接口")
public class HealthController {

    /**
     * 健康检查端点
     *
     * @return 健康状态响应
     */
    @Operation(summary = "系统健康检查", description = "检查系统运行状态，返回服务状态、时间戳、服务名称和版本信息")
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
     *
     * @return WebSocket连接状态
     */
    @Operation(summary = "获取WebSocket连接信息", description = "返回WebSocket连接端点、主题和状态信息")
    @GetMapping("/websocket")
    public WebSocketResponse<Map<String, Object>> websocketInfo() {
        Map<String, Object> wsInfo = new HashMap<>();
        wsInfo.put("endpoint", "/ws");
        wsInfo.put("topic", "/topic/sensor-data");
        wsInfo.put("status", "enabled");
        
        return WebSocketResponse.success(wsInfo);
    }
}