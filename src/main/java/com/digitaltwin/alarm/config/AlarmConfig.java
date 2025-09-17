package com.digitaltwin.alarm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "alarm")
public class AlarmConfig {
    /**
     * 告警防重复时间间隔（分钟）
     * 默认值为5分钟
     */
    private Integer duplicatePreventionMinutes = 60;
}