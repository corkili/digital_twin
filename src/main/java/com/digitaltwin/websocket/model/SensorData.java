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
    
    @JsonProperty("HeatFlux")
    private Double HeatFlux;
    
    @JsonProperty("CoolingWater_In_Temp")
    private Double CoolingWaterInTemp;
    
    @JsonProperty("Timestamp")
    private Long Timestamp;
    
    // 用于存储未知字段的Map
    private Map<String, Object> unknownFields = new HashMap<>();
    
    @JsonAnySetter
    public void setUnknownField(String key, Object value) {
        if (unknownFields == null) {
            unknownFields = new HashMap<>();
        }
        unknownFields.put(key, value);
    }

    public boolean IsValidSensorData() {
        return this.HeatFlux != null && this.CoolingWaterInTemp != null;
    }
}