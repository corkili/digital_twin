package com.digitaltwin.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 数字孪生WebSocket服务器主应用类
 * 启动Spring Boot应用程序
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.digitaltwin.websocket", "com.digitaltwin.device"})
public class DigitalTwinWebsocketApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitalTwinWebsocketApplication.class, args);
    }
}