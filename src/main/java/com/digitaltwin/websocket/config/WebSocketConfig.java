package com.digitaltwin.websocket.config;

import com.digitaltwin.websocket.interceptor.WebSocketAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置类
 * 配置WebSocket消息代理和端点
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单的消息代理，将消息发送到以"/topic"开头的目的地
        config.enableSimpleBroker("/topic");
        // 设置应用程序前缀为"/app"
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册WebSocket端点，客户端将连接到此端点
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // 允许跨域
                .addInterceptors(webSocketAuthInterceptor) // 添加认证拦截器
                .withSockJS(); // 启用SockJS作为备选方案
    }
}