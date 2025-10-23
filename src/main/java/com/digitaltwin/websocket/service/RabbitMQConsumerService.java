package com.digitaltwin.websocket.service;

import com.digitaltwin.alarm.service.AlarmAnalysisService;
import com.digitaltwin.alarm.service.PointCacheService;
import com.digitaltwin.device.entity.Point;
import com.digitaltwin.device.entity.PointFailureRecord;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final PointCacheService pointCacheService;
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

            // 1. 获取原始 Map
            Map<String, Object> rawMap = sensorData.getPointDataMap();
            if (rawMap == null) {
                return; // 空 Map 直接返回
            }

            // 2. 处理 Map 中的数值，保留 2 位小数
            Map<String, Object> processedMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : rawMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                // 只处理数值类型（double、float、Integer 等）
                if (value instanceof Number) {
                    // 转换为 BigDecimal 并设置精度（四舍五入）
                    BigDecimal num = new BigDecimal(value.toString())
                            .setScale(2, RoundingMode.HALF_UP); // 保留 2 位小数
                    processedMap.put(key, num);
                } else {
                    // 非数值类型直接保留（如字符串、布尔值等）
                    processedMap.put(key, value);
                }
            }

            // 3. 将处理后的 Map 重新设置到 SensorData 中
            sensorData.setPointDataMap(processedMap);


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
            
            // 创建过滤后的SensorData对象，只包含已发布的点位数据
            SensorData filteredSensorData = filterPublishedPoints(sensorData);
            
            // 如果过滤后还有数据，则推送到WebSocket
            if (filteredSensorData != null && filteredSensorData.IsValidSensorData()) {
                // 创建WebSocket响应
                WebSocketResponse<SensorData> response = WebSocketResponse.success(filteredSensorData.NewDataWithFormatDecimal());
                
                // 推送到WebSocket
                webSocketPushService.pushToSubscribers(response);
                
                log.debug("成功推送已发布点位数据到WebSocket，共推送 {} 个点位", filteredSensorData.getPointDataMap().size());
            } else {
                log.debug("没有已发布的点位数据需要推送");
            }
            
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
                
                // 从缓存中查找identity为EStop的点位
                List<Point> points = pointCacheService.getPointsByIdentity("EStop");
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
     * 过滤未发布的点位数据，只保留已发布的点位
     *
     * @param sensorData 原始传感器数据
     * @return 过滤后的传感器数据，只包含已发布的点位
     */
    private SensorData filterPublishedPoints(SensorData sensorData) {
        if (sensorData == null || sensorData.getPointDataMap() == null) {
            return null;
        }
        
        // 创建新的SensorData对象
        SensorData filteredData = new SensorData();
        filteredData.setID(sensorData.getID());
        filteredData.setTimestamp(sensorData.getTimestamp());
        filteredData.setDeviceName(sensorData.getDeviceName());
        filteredData.setTs(sensorData.getTs());
        filteredData.setDeviceType(sensorData.getDeviceType());
        
        // 保存已发布的点位数据
        Map<String, Object> filteredPointDataMap = new HashMap<>();
        
        // 遍历所有点位数据
        for (Map.Entry<String, Object> entry : sensorData.getPointDataMap().entrySet()) {
            String pointIdentity = entry.getKey();
            
            // 检查是否是特殊点位(TestPhase和EStop)，这些点位始终保留
            if ("TestPhase".equals(pointIdentity) || "EStop".equals(pointIdentity)) {
                filteredPointDataMap.put(pointIdentity, entry.getValue());
                continue;
            }
            
            // 从缓存中获取点位信息
            List<Point> points = pointCacheService.getPointsByIdentity(pointIdentity);
            
            // 检查点位是否存在且已发布
            if (!points.isEmpty()) {
                boolean anyPublished = points.stream().anyMatch(Point::getPublished);
                if (anyPublished) {
                    filteredPointDataMap.put(pointIdentity, entry.getValue());
                } else {
                    log.debug("过滤掉未发布的点位: {}", pointIdentity);
                }
            } else {
                log.debug("未找到点位信息，过滤点位: {}", pointIdentity);
            }
        }
        
        filteredData.setPointDataMap(filteredPointDataMap);
        return filteredData;
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