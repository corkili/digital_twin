package com.digitaltwin.trial.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "trail.history.cache")
public class TrailHistoryCacheConfig {
    /**
     * 缓存过期时间（毫秒），默认30分钟
     */
    private long expireAfterWrite = 30 * 60 * 1000L;
    
    /**
     * 缓存最大条目数，默认1000
     */
    private int maximumSize = 1000;
}