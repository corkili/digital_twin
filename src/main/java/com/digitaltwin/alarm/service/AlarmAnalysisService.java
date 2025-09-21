package com.digitaltwin.alarm.service;

import com.digitaltwin.alarm.config.AlarmConfig;
import com.digitaltwin.alarm.dto.AlarmNotificationDTO;
import com.digitaltwin.alarm.entity.Alarm;
import com.digitaltwin.alarm.repository.AlarmRepository;
import com.digitaltwin.device.entity.Point;
import com.digitaltwin.websocket.model.SensorData;
import com.digitaltwin.websocket.service.WebSocketPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmAnalysisService {

    private final PointCacheService pointCacheService;
    private final AlarmRepository alarmRepository;
    private final WebSocketPushService webSocketPushService;
    private final AlarmConfig alarmConfig;
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * 分析传感器数据并生成告警
     * 
     * @param sensorData 传感器数据
     */
    @Async
    public void analyzeSensorData(SensorData sensorData) {
        try {
            log.info("开始分析传感器数据告警: {}", sensorData.getID());
            
            if (sensorData.getPointDataMap() == null || sensorData.getPointDataMap().isEmpty()) {
                log.warn("传感器数据点位数据为空: {}", sensorData.getID());
                return;
            }
            
            // 对每个点位数据进行异步分析
            sensorData.getPointDataMap().forEach((pointIdentity, pointValue) -> {
                CompletableFuture.runAsync(() -> 
                    analyzePointData(sensorData, pointIdentity, pointValue), executorService);
            });
            
        } catch (Exception e) {
            log.error("分析传感器数据时发生错误: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 分析单个点位数据
     * 
     * @param sensorData 传感器数据
     * @param pointIdentity 点位标识
     * @param pointValue 点位值
     */
    private void analyzePointData(SensorData sensorData, String pointIdentity, Object pointValue) {
        try {
            List<Point> points = pointCacheService.getPointsByIdentity(pointIdentity);
            if (points.isEmpty()) {
                log.debug("缓存中未找到点位: {}，跳过告警分析", pointIdentity);
                return;
            }
            
            String stringValue = String.valueOf(pointValue);
            
            // 对每个匹配的点位都进行告警分析
            for (Point point : points) {
                if (!Boolean.TRUE.equals(point.getAlarmable())) {
                    log.debug("点位未启用告警: {}", pointIdentity);
                    continue;
                }
                
                // 根据点位值类型进行告警分析
                if (pointValue instanceof Boolean) {
                    analyzeBooleanAlarm(sensorData, point, (Boolean) pointValue, stringValue);
                } else if (pointValue instanceof Number) {
                    analyzeNumericAlarm(sensorData, point, ((Number) pointValue).doubleValue(), stringValue);
                } else {
                    log.debug("字符串类型点位不进行分析: {}", pointIdentity);
                }
            }
            
        } catch (Exception e) {
            log.error("分析点位数据时发生错误: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 分析布尔类型告警
     * 
     * @param sensorData 传感器数据
     * @param point 点位
     * @param value 点位值
     * @param stringValue 字符串值
     */
    private void analyzeBooleanAlarm(SensorData sensorData, Point point, Boolean value, String stringValue) {
        if (point.getStateAlarm() != null && value.equals(point.getStateAlarm())) {
            // 检查是否存在未结束的告警
            List<Alarm> unendedAlarms = alarmRepository.findUnendedAlarmsByPointIdAndAlarmType(
                    point.getIdentity(), "状态告警");
            
            if (unendedAlarms.isEmpty()) {
                // 如果不存在未结束的告警，则创建新的告警
                createAlarm(sensorData, point, stringValue, "状态告警", String.valueOf(point.getStateAlarm()));
            } else {
                // 如果存在未结束的告警，则更新lastSensorTimestamp
                for (Alarm alarm : unendedAlarms) {
                    alarm.setLastSensorTimestamp(sensorData.getRealTimestamp());
                    alarmRepository.save(alarm);
                }
            }
        } else {
            // 如果不满足告警条件，检查是否存在未结束的告警并结束它
            checkAndEndAlarm(sensorData, point, "状态告警");
        }
    }
    
    /**
     * 分析数值类型告警
     * 
     * @param sensorData 传感器数据
     * @param point 点位
     * @param value 点位值
     * @param stringValue 字符串值
     */
    private void analyzeNumericAlarm(SensorData sensorData, Point point, Double value, String stringValue) {
        boolean alarmTriggered = false;
        
        // 检查上上限告警
        if (point.getUpperHighLimit() != null && value > point.getUpperHighLimit()) {
            handleNumericAlarm(sensorData, point, stringValue, "上上限告警", point.getUpperHighLimit());
            alarmTriggered = true;
        }
        
        // 检查上限告警
        if (point.getUpperLimit() != null && value > point.getUpperLimit()) {
            handleNumericAlarm(sensorData, point, stringValue, "上限告警", point.getUpperLimit());
            alarmTriggered = true;
        }
        
        // 检查下下限告警
        if (point.getLowerLowLimit() != null && value < point.getLowerLowLimit()) {
            handleNumericAlarm(sensorData, point, stringValue, "下下限告警", point.getLowerLowLimit());
            alarmTriggered = true;
        }
        
        // 检查下限告警
        if (point.getLowerLimit() != null && value < point.getLowerLimit()) {
            handleNumericAlarm(sensorData, point, stringValue, "下限告警", point.getLowerLimit());
            alarmTriggered = true;
        }
        
        // 如果没有触发任何告警，检查是否存在未结束的告警并结束它
        if (!alarmTriggered) {
            checkAndEndAlarm(sensorData, point, "数值告警");
        }
    }
    
    /**
     * 处理数值类型告警
     * 
     * @param sensorData 传感器数据
     * @param point 点位
     * @param pointValue 点位值
     * @param alarmType 告警类型
     * @param alarmThreshold 告警阈值
     */
    private void handleNumericAlarm(SensorData sensorData, Point point, String pointValue, String alarmType, Double alarmThreshold) {
        // 检查是否存在未结束的告警
        List<Alarm> unendedAlarms = alarmRepository.findUnendedAlarmsByPointIdAndAlarmType(
                point.getIdentity(), alarmType);
        
        if (unendedAlarms.isEmpty()) {
            // 如果不存在未结束的告警，则创建新的告警
            createAlarm(sensorData, point, pointValue, alarmType, String.valueOf(alarmThreshold));
        } else {
            // 如果存在未结束的告警，则更新lastSensorTimestamp
            for (Alarm alarm : unendedAlarms) {
                alarm.setLastSensorTimestamp(sensorData.getRealTimestamp());
                alarmRepository.save(alarm);
            }
        }
    }
    
    /**
     * 检查并结束告警
     * 
     * @param sensorData 传感器数据
     * @param point 点位
     * @param alarmType 告警类型
     */
    private void checkAndEndAlarm(SensorData sensorData, Point point, String alarmType) {
        List<Alarm> unendedAlarms = new ArrayList<>();
        
        // 根据不同的告警类型查询未结束的告警
        switch (alarmType) {
            case "状态告警":
                unendedAlarms = alarmRepository.findUnendedAlarmsByPointIdAndAlarmType(
                        point.getIdentity(), alarmType);
                break;
            case "数值告警":
                // 对于数值类型的告警，我们需要查询所有数值类型的未结束告警
                // 这里简化处理，查询所有数值类型的告警
                unendedAlarms = alarmRepository.findUnendedAlarmsByPointIdAndAlarmType(
                        point.getIdentity(), "上上限告警");
                unendedAlarms.addAll(alarmRepository.findUnendedAlarmsByPointIdAndAlarmType(
                        point.getIdentity(), "上限告警"));
                unendedAlarms.addAll(alarmRepository.findUnendedAlarmsByPointIdAndAlarmType(
                        point.getIdentity(), "下下限告警"));
                unendedAlarms.addAll(alarmRepository.findUnendedAlarmsByPointIdAndAlarmType(
                        point.getIdentity(), "下限告警"));
                break;
            default:
                // 默认情况，查询指定类型的未结束告警
                unendedAlarms = alarmRepository.findUnendedAlarmsByPointIdAndAlarmType(
                        point.getIdentity(), alarmType);
                break;
        }
        
        // 更新所有未结束的告警
        for (Alarm alarm : unendedAlarms) {
            alarm.setEndTimestamp(System.currentTimeMillis());
            alarmRepository.save(alarm);
        }
    }
    
    /**
     * 创建告警记录
     * 
     * @param sensorData 传感器数据
     * @param point 点位
     * @param pointValue 点位值
     * @param alarmType 告警类型
     * @param alarmThreshold 告警阈值
     */
    private void createAlarm(SensorData sensorData, Point point, String pointValue, 
                           String alarmType, String alarmThreshold) {
        try {
            // 计算时间窗口：过去N分钟
            long currentTime = System.currentTimeMillis();
            long startTime = currentTime - (alarmConfig.getDuplicatePreventionMinutes() * 60 * 1000L);
            
            // 查询在时间窗口内是否已存在该点位的告警（不论是否结束）
            Long alarmCount = alarmRepository.countRecentAlarmsByPointId(
                    point.getIdentity(), startTime);
            
            // 如果在时间窗口内已存在告警，则不创建新的告警
            if (alarmCount > 0) {
                log.debug("在过去{}分钟内已存在点位{}的告警，跳过创建新告警", 
                        alarmConfig.getDuplicatePreventionMinutes(), point.getIdentity());
                return;
            }

            Alarm alarm = new Alarm();
            alarm.setTimestamp(currentTime); // 使用当前时间作为告警产生时间
            alarm.setSensorId(sensorData.getID());
            alarm.setSensorTimestamp(sensorData.getRealTimestamp());
            alarm.setPointId(point.getIdentity());
            alarm.setAlarmType(alarmType);
            alarm.setAlarmThreshold(alarmThreshold);
            alarm.setDeviceId(point.getDevice().getId());
            alarm.setLastSensorTimestamp(sensorData.getRealTimestamp()); // 设置lastSensorTimestamp
            
            try {
                BigDecimal bd = new BigDecimal(pointValue);
                String pv = bd.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                alarm.setPointValue(pv);
            } catch (NumberFormatException e) {
                // 如果点位值不是数值，设置为null
                alarm.setPointValue(pointValue);
            }

            alarmRepository.save(alarm);
            
            log.info("生成告警: 设备={}, 点位={}, 类型={}, 值={}, 阈值={}", 
                    point.getDevice().getName(), point.getIdentity(), alarmType, pointValue, alarmThreshold);
            
            // 通过WebSocket推送告警通知
            pushAlarmNotification(alarm, point.getDevice().getName(), point.getIdentity());
            
        } catch (Exception e) {
            log.error("创建告警记录时发生错误: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 推送告警通知到WebSocket
     * 
     * @param alarm 告警实体
     * @param deviceName 设备名称
     * @param pointIdentity 点位标识
     */
    private void pushAlarmNotification(Alarm alarm, String deviceName, String pointIdentity) {
        try {
            AlarmNotificationDTO alarmNotification = new AlarmNotificationDTO();
            alarmNotification.setAlarmId(alarm.getId());
            alarmNotification.setDeviceId(alarm.getDeviceId());
            alarmNotification.setDeviceName(deviceName);
            alarmNotification.setAlarmType(alarm.getAlarmType());
            alarmNotification.setPointIdentity(pointIdentity);
            
            webSocketPushService.pushAlarmToSubscribers(
                com.digitaltwin.websocket.model.WebSocketResponse.success(alarmNotification)
            );
            
            log.debug("告警已推送到WebSocket主题: /topic/alarms");
        } catch (Exception e) {
            log.error("推送告警通知时发生错误: {}", e.getMessage(), e);
        }
    }

}