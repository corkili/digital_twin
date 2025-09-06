package com.digitaltwin.websocket.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
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
}