package com.digitaltwin.websocket.controller;

import com.digitaltwin.websocket.model.SensorData;
import com.digitaltwin.websocket.model.WebSocketResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 传感器数据控制器
 * 提供REST API接口处理传感器数据的发送和查询
 */
@Slf4j
@RestController
@RequestMapping("/sensor")
@RequiredArgsConstructor
public class SensorController {

    private final RabbitTemplate rabbitTemplate;
    private final String SENSOR_DATA_EXCHANGE = "sensor.data.exchange";
    private final String SENSOR_DATA_ROUTING_KEY = "sensor.data";

    /**
     * 发送传感器数据到RabbitMQ
     * 
     * @param sensorData 传感器数据
     * @return 发送结果
     */
    @PostMapping("/send")
    public WebSocketResponse<String> sendSensorData(@RequestBody SensorData sensorData) {
        try {
            // 设置唯一ID和时间戳
            if (sensorData.getID() == null || sensorData.getID().isEmpty()) {
                sensorData.setID("sensor-" + UUID.randomUUID().toString());
            }
            sensorData.setTimestamp(System.currentTimeMillis());
            
            log.info("接收到传感器数据发送请求: {}", sensorData);
            
            // 发送到RabbitMQ
            rabbitTemplate.convertAndSend(
                SENSOR_DATA_EXCHANGE,
                SENSOR_DATA_ROUTING_KEY,
                sensorData
            );

            log.info("传感器数据已发送到RabbitMQ: {}", sensorData.getID());
            
            return WebSocketResponse.success("传感器数据已发送到消息队列");
            
        } catch (Exception e) {
            log.error("发送传感器数据到RabbitMQ失败: {}", e.getMessage(), e);
            return WebSocketResponse.error("发送失败: " + e.getMessage());
        }
    }

    /**
     * 获取传感器历史数据（模拟数据）
     * 
     * @param limit 返回记录数量限制
     * @return 传感器数据列表
     */
    @GetMapping("/history")
    public WebSocketResponse<List<SensorData>> getSensorHistory(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            log.info("获取传感器历史数据请求，限制数量: {}", limit);
            
            // 这里应该是从数据库或缓存中获取历史数据
            // 目前返回模拟数据
            List<SensorData> history = List.of(
                createMockSensorData("SENSOR-001", 39.77, "°C", "Room-101", "normal"),
                createMockSensorData("SENSOR-002", 38.21, "°C", "Room-102", "normal"),
                createMockSensorData("SENSOR-003", 40.15, "°C", "Room-103", "normal")
            );
            
            return WebSocketResponse.success(history.stream()
                .limit(limit)
                .collect(java.util.stream.Collectors.toList()));
                
        } catch (Exception e) {
            log.error("获取传感器历史数据失败: {}", e.getMessage(), e);
            return WebSocketResponse.error("获取历史数据失败: " + e.getMessage());
        }
    }

    /**
     * 创建模拟传感器数据
     */
    private SensorData createMockSensorData(String sensorId, Double coolingWaterInTemp, String unit, 
                                          String location, String status) {
        SensorData data = new SensorData();
        data.setID("sensor-" + UUID.randomUUID().toString());
        data.setHeatFlux(113.93);
        data.setCoolingWaterInTemp(coolingWaterInTemp);
        data.setTimestamp(System.currentTimeMillis());
        return data;
    }
}