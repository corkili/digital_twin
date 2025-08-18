package com.digitaltwin.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 数字孪生WebSocket服务器主应用类
 * 启动Spring Boot应用程序
 */
@SpringBootApplication
public class DigitalTwinWebsocketApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitalTwinWebsocketApplication.class, args);
    }
}