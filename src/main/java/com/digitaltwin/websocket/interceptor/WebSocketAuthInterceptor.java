package com.digitaltwin.websocket.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * WebSocket认证拦截器
 * 用于WebSocket连接时的身份验证
 */
@Slf4j
@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private static final String USER_SESSION_KEY = "currentUser";

    /**
     * 在WebSocket握手前进行身份验证
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // 将ServerHttpRequest转换为ServletServerHttpRequest
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            HttpServletRequest httpServletRequest = servletRequest.getServletRequest();
            HttpSession session = httpServletRequest.getSession(false);
            
            // 检查用户是否已登录
            if (session == null || session.getAttribute(USER_SESSION_KEY) == null) {
                log.warn("WebSocket连接认证失败: 用户未登录");
                return false;
            }
            
            // 将用户名添加到WebSocket会话属性中
            String username = ((com.digitaltwin.websocket.model.User) session.getAttribute(USER_SESSION_KEY)).getUsername();
            attributes.put("username", username);
            log.debug("WebSocket连接认证成功: 用户 {}", username);
        }
        
        return true;
    }

    /**
     * WebSocket握手后调用
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                              WebSocketHandler wsHandler, Exception exception) {
        // 握手后的处理逻辑，可以省略
    }
}