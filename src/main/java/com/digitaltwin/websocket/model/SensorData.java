package com.digitaltwin.websocket.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 传感器数据模型类
 * 用于接收从RabbitMQ消费的JSON格式数据
 * 支持未知字段的收集
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
public class SensorData {
    
    @JsonProperty("ID")
    private String ID;
    
    @JsonProperty("Timestamp")
    private Long Timestamp;

    @JsonProperty("deviceName")
    private String deviceName;

    @JsonProperty("ts")
    private String ts;

    @JsonProperty("deviceType")
    private String deviceType;
    
    // 用于存储点位数据的Map
    private Map<String, Object> PointDataMap = new HashMap<>();
    
    @JsonAnySetter
    public void setPointData(String key, Object value) {
        if (PointDataMap == null) {
            PointDataMap = new HashMap<>();
        }
        PointDataMap.put(key, value);
    }

    public boolean IsValidSensorData() {
        return this.PointDataMap != null && !this.PointDataMap.isEmpty();
    }

    public Long getRealTimestamp() {
        Long timestamp = null;
        if (this.ts != null) {
            try {
                timestamp = Long.parseLong(this.ts);
            } catch (NumberFormatException e) {
                log.warn("无法解析ts字段为时间戳: {}", this.ts);
            }
        }
            
        // 如果无法从SensorData获取ts，则使用SensorData的Timestamp字段
        if (timestamp == null) {
            timestamp = this.Timestamp;
        }
        return timestamp;
    }
}