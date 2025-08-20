package com.digitaltwin.websocket.service;

import com.digitaltwin.websocket.config.RabbitMQConfig;
import com.digitaltwin.websocket.model.SensorData;
import com.digitaltwin.websocket.model.WebSocketResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * RabbitMQ消费者服务
 * 负责从RabbitMQ消费消息并推送到WebSocket
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQConsumerService {

    private final WebSocketPushService webSocketPushService;
    private final TDengineService tdengineService;
    /**
     * 监听传感器数据队列
     * 确保按消费顺序推送消息到WebSocket
     * 
     * @param sensorData 从RabbitMQ接收的传感器数据
     */
    @RabbitListener(queues = RabbitMQConfig.SENSOR_DATA_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void receiveSensorData(SensorData sensorData) {
        try {

            log.info("接收到传感器数据: {}", sensorData);

            if (!sensorData.IsValidSensorData()) {
                log.warn("传感器数据无效: {}", sensorData);
                return;
            }

            // 临时增加id和timestamp
            if (sensorData.getID() == null || sensorData.getID().isEmpty()) {
                sensorData.setID("sensor-" + UUID.randomUUID().toString());
            }
            sensorData.setTimestamp(System.currentTimeMillis());

            // 保存到TDengine
            tdengineService.saveSensorData(sensorData);
            
            // 创建WebSocket响应
            WebSocketResponse<SensorData> response = WebSocketResponse.success(sensorData);
            
            // 推送到WebSocket
            webSocketPushService.pushToSubscribers(response);


            
            log.debug("成功推送传感器数据到WebSocket: {}", sensorData);
            
        } catch (Exception e) {
            log.error("处理传感器数据时发生错误: {}", e.getMessage(), e);
        }
    }
}