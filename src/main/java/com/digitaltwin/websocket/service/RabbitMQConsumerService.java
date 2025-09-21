package com.digitaltwin.websocket.service;

import com.digitaltwin.alarm.service.AlarmAnalysisService;
import com.digitaltwin.device.entity.Point;
import com.digitaltwin.device.entity.PointFailureRecord;
import com.digitaltwin.device.repository.PointRepository;
import com.digitaltwin.device.service.PointCollectionStatsService;
import com.digitaltwin.device.service.PointFailureRecordService;
import com.digitaltwin.websocket.config.RabbitMQConfig;
import com.digitaltwin.websocket.model.SensorData;
import com.digitaltwin.websocket.model.TestPhaseItem;
import com.digitaltwin.websocket.model.TestPhaseResponse;
import com.digitaltwin.websocket.model.WebSocketResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * RabbitMQ消费者服务
 * 负责从RabbitMQ消费消息并推送到WebSocket
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQConsumerService {

    // 测试阶段列表常量
    private static final List<String> TEST_PHASES = Arrays.asList(
        "模型安装",
        "打开电源",
        "注入氛围气体",
        "开启冷却水/气阀门",
        "石英灯阵加热",
        "实时监控试验过程",
        "石英灯阵加热停止",
        "关闭冷却水/气阀门"
    );

    private final WebSocketPushService webSocketPushService;
    private final TDengineService tdengineService;
    private final AlarmAnalysisService alarmAnalysisService;
    private final PointCollectionStatsService pointCollectionStatsService;
    private final PointRepository pointRepository;
    private final PointFailureRecordService pointFailureRecordService;

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

            // 检查EStop点位值变化
            checkEStopPoint(sensorData);

            // 检查TestPhase字段变化
            checkTestPhase(sensorData);

            // 临时增加id和timestamp
            if (sensorData.getID() == null || sensorData.getID().isEmpty()) {
                sensorData.setID("sensor-" + UUID.randomUUID().toString());
            }
            sensorData.setTimestamp(System.currentTimeMillis());

            // 保存到TDengine
            tdengineService.saveSensorData(sensorData);
            
            // 更新点位采集统计信息
            pointCollectionStatsService.updatePointCollectionStats(sensorData);
            
            // 异步进行告警分析
            alarmAnalysisService.analyzeSensorData(sensorData);
            
            // 创建WebSocket响应
            WebSocketResponse<SensorData> response = WebSocketResponse.success(sensorData);
            
            // 推送到WebSocket
            webSocketPushService.pushToSubscribers(response);

            
            log.debug("成功推送传感器数据到WebSocket: {}", sensorData);
            
        } catch (Exception e) {
            log.error("处理传感器数据时发生错误: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 检查EStop点位值变化，处理故障开始和结束时间
     * 
     * @param sensorData 传感器数据
     */
    private void checkEStopPoint(SensorData sensorData) {
        try {
            // 检查是否存在EStop点位
            if (sensorData.getPointDataMap() != null && 
                sensorData.getPointDataMap().containsKey("EStop")) {
                
                Object estopValue = sensorData.getPointDataMap().get("EStop");
                
                // 查找identity为EStop的点位
                List<Point> points = pointRepository.findByIdentity("EStop");
                if (!points.isEmpty()) {
                    Point point = points.get(0); // 取第一个匹配的点位
                    
                    if (Boolean.TRUE.equals(estopValue)) {
                        // EStop为true，检查是否已存在未解决的故障记录
                        PointFailureRecord activeRecord = pointFailureRecordService.getActiveFailureRecord(point.getId());
                        if (activeRecord == null) {
                            // 没有活动的故障记录，创建新的故障记录
                            pointFailureRecordService.recordFailure(
                                point.getId(), 
                                "EStop点位值为true，触发紧急停止状态，值为: " + estopValue,
                                    estopValue.toString()
                            );
                            log.info("检测到EStop点位值为true，已记录故障开始时间，点位ID: {}", point.getId());
                        }
                    } else if (Boolean.FALSE.equals(estopValue) || estopValue == null) {
                        // EStop为false或null，检查是否有活动的故障记录需要结束
                        PointFailureRecord activeRecord = pointFailureRecordService.getActiveFailureRecord(point.getId());
                        if (activeRecord != null) {
                            // 更新故障记录的结束时间
                            pointFailureRecordService.resolveFailure(
                                activeRecord.getId(),
                                "EStop点位值恢复为false，故障结束，值为: " + estopValue
                            );
                            log.info("检测到EStop点位值为false，已更新故障结束时间，记录ID: {}", activeRecord.getId());
                        }
                    }
                } else {
                    log.warn("未找到identity为EStop的点位");
                }
            }
        } catch (Exception e) {
            log.error("检查EStop点位时发生错误: {}", e.getMessage(), e);
        }
    }

    /**
     * 检查TestPhase字段值变化，推送测试阶段信息
     *
     * @param sensorData 传感器数据
     */
    private void checkTestPhase(SensorData sensorData) {
        try {
            // 检查是否存在TestPhase字段
            if (sensorData.getPointDataMap() != null &&
                sensorData.getPointDataMap().containsKey("TestPhase")) {

                Object testPhaseValue = sensorData.getPointDataMap().get("TestPhase");

                if (testPhaseValue != null) {
                    String testPhaseStr = testPhaseValue.toString();

                    // 检查是否在预定义列表中
                    if (TEST_PHASES.contains(testPhaseStr)) {
                        // 创建测试阶段列表，标记当前阶段
                        List<TestPhaseItem> phases = TEST_PHASES.stream()
                            .map(phase -> new TestPhaseItem(phase, phase.equals(testPhaseStr)))
                            .collect(Collectors.toList());

                        // 创建响应对象
                        TestPhaseResponse response = new TestPhaseResponse(
                            phases,
                            testPhaseStr,
                            System.currentTimeMillis()
                        );

                        // 推送到WebSocket
                        webSocketPushService.pushTestPhaseToSubscribers(
                            WebSocketResponse.success(response)
                        );

                        log.info("检测到TestPhase变化，当前阶段: {}", testPhaseStr);
                    } else {
                        log.debug("TestPhase值不在预定义列表中: {}", testPhaseStr);
                    }
                }
            }
        } catch (Exception e) {
            log.error("检查TestPhase字段时发生错误: {}", e.getMessage(), e);
        }
    }
}