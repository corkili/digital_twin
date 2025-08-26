package com.digitaltwin.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 数字孪生WebSocket服务器主应用类
 * 启动Spring Boot应用程序
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.digitaltwin.websocket", "com.digitaltwin.device"})
@EnableJpaRepositories(basePackages = "com.digitaltwin.device.repository")
@EntityScan(basePackages = "com.digitaltwin.device.entity")
public class DigitalTwinWebsocketApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitalTwinWebsocketApplication.class, args);
    }
}