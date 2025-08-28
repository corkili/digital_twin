package com.digitaltwin.alarm.service;

import com.digitaltwin.alarm.entity.Alarm;
import com.digitaltwin.alarm.repository.AlarmRepository;
import com.digitaltwin.device.entity.Point;
import com.digitaltwin.websocket.model.SensorData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmAnalysisService {

    private final PointCacheService pointCacheService;
    private final AlarmRepository alarmRepository;
    
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
            createAlarm(sensorData, point, stringValue, "状态告警", String.valueOf(point.getStateAlarm()));
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
        // 检查上上限告警
        if (point.getUpperHighLimit() != null && value > point.getUpperHighLimit()) {
            createAlarm(sensorData, point, stringValue, "上上限告警", String.valueOf(point.getUpperHighLimit()));
        }
        
        // 检查上限告警
        if (point.getUpperLimit() != null && value > point.getUpperLimit()) {
            createAlarm(sensorData, point, stringValue, "上限告警", String.valueOf(point.getUpperLimit()));
        }
        
        // 检查下下限告警
        if (point.getLowerLowLimit() != null && value < point.getLowerLowLimit()) {
            createAlarm(sensorData, point, stringValue, "下下限告警", String.valueOf(point.getLowerLowLimit()));
        }
        
        // 检查下限告警
        if (point.getLowerLimit() != null && value < point.getLowerLimit()) {
            createAlarm(sensorData, point, stringValue, "下限告警", String.valueOf(point.getLowerLimit()));
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
            Alarm alarm = new Alarm();
            alarm.setTimestamp(System.currentTimeMillis());
            alarm.setSensorId(sensorData.getID());
            alarm.setSensorTimestamp(sensorData.getTimestamp());
            alarm.setPointId(point.getIdentity());
            alarm.setPointValue(pointValue);
            alarm.setAlarmType(alarmType);
            alarm.setAlarmThreshold(alarmThreshold);
            alarm.setDeviceId(point.getDevice().getId());
            
            alarmRepository.save(alarm);
            
            log.info("生成告警: 设备={}, 点位={}, 类型={}, 值={}, 阈值={}", 
                    point.getDevice().getName(), point.getIdentity(), alarmType, pointValue, alarmThreshold);
            
        } catch (Exception e) {
            log.error("创建告警记录时发生错误: {}", e.getMessage(), e);
        }
    }
}