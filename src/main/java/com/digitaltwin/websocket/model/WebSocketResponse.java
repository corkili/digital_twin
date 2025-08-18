package com.digitaltwin.websocket.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * WebSocket响应数据模型
 * 用于推送给前端的数据协议
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketResponse<T> {
    
    @JsonProperty("code")
    private int code = 200;
    
    @JsonProperty("message")
    private String message = "success";
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp = LocalDateTime.now();
    
    @JsonProperty("data")
    private T data;
    
    public static <T> WebSocketResponse<T> success(T data) {
        return new WebSocketResponse<>(200, "success", LocalDateTime.now(), data);
    }
    
    public static <T> WebSocketResponse<T> error(String message) {
        return new WebSocketResponse<>(500, message, LocalDateTime.now(), null);
    }
}