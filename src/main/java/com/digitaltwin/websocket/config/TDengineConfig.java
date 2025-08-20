package com.digitaltwin.websocket.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "tdengine")
public class TDengineConfig {
    private String url;
    private String username;
    private String password;
}
