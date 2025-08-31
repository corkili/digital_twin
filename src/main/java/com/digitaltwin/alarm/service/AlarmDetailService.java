package com.digitaltwin.alarm.service;

import com.digitaltwin.alarm.dto.AlarmDetailResponse;
import com.digitaltwin.alarm.entity.Alarm;
import com.digitaltwin.alarm.entity.AlarmOperateLog;
import com.digitaltwin.alarm.repository.AlarmOperateLogRepository;
import com.digitaltwin.alarm.repository.AlarmRepository;
import com.digitaltwin.device.entity.Point;
import com.digitaltwin.device.repository.PointRepository;
import com.digitaltwin.device.entity.Device;
import com.digitaltwin.device.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import com.digitaltwin.alarm.service.AlarmQueryService;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmDetailService {
    
    private final AlarmRepository alarmRepository;
    private final AlarmOperateLogRepository alarmOperateLogRepository;
    private final PointRepository pointRepository;
    private final DeviceRepository deviceRepository;
    private final AlarmQueryService alarmQueryService;
    
    public AlarmDetailResponse getAlarmDetail(Long alarmId, boolean needOperateLog, Integer operateLogLimit) {
        try {
            // 查询告警信息
            Optional<Alarm> alarmOptional = alarmRepository.findById(alarmId);
            if (!alarmOptional.isPresent()) {
                throw new RuntimeException("未找到ID为 " + alarmId + " 的告警");
            }
            
            Alarm alarm = alarmOptional.get();
            AlarmDetailResponse response = new AlarmDetailResponse();
            
            // 设置告警信息
            response.setAlarmId(alarm.getId());
            response.setTimestamp(alarm.getTimestamp());
            response.setFormattedTimestamp(formatTimestamp(alarm.getTimestamp()));
            response.setSensorTimestamp(alarm.getSensorTimestamp());
            response.setFormattedSensorTimestamp(formatTimestamp(alarm.getSensorTimestamp()));
            response.setAlarmType(alarm.getAlarmType());
            response.setAlarmThreshold(alarm.getAlarmThreshold());
            response.setAlarmState(alarm.getState().name());
            
            // 查询关联的点位信息
            Optional<Point> pointOptional = pointRepository.findByIdentityAndDeviceId(alarm.getPointId(), alarm.getDeviceId());
            if (pointOptional.isPresent()) {
                Point point = pointOptional.get();
                response.setPointId(point.getId());
                response.setPointIdentity(point.getIdentity());
                response.setPointPath(point.getPath());
                response.setPointUnit(point.getUnit());
                
                // 查询关联的设备信息
                Device device = point.getDevice();
                if (device != null) {
                    response.setDeviceId(device.getId());
                    response.setDeviceName(device.getName());
                }
            }
            
            // 查询操作日志列表
            if (needOperateLog) {
                if (operateLogLimit != null && operateLogLimit > 0) {
                    Pageable pageable = PageRequest.of(0, operateLogLimit);
                    response.setOperateLogs(alarmOperateLogRepository.findByAlarmIdOrderByOperateTimeDesc(alarmId, pageable));
                } else {
                    response.setOperateLogs(alarmOperateLogRepository.findByAlarmIdOrderByOperateTimeDesc(alarmId));
                }
            }
            
            return response;
        } catch (Exception e) {
            log.error("获取告警详情失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取告警详情失败: " + e.getMessage(), e);
        }
    }
    
    public AlarmDetailResponse getLatestAlarmDetailByDeviceId(Long deviceId) {
        try {
            // 查询最新告警
            Alarm latestAlarm = alarmQueryService.getLatestAlarmByDeviceId(deviceId);
            if (latestAlarm == null) {
                throw new RuntimeException("未找到设备ID为 " + deviceId + " 的告警");
            }
            
            AlarmDetailResponse response = new AlarmDetailResponse();
            
            // 设置告警信息
            response.setAlarmId(latestAlarm.getId());
            response.setTimestamp(latestAlarm.getTimestamp());
            response.setFormattedTimestamp(formatTimestamp(latestAlarm.getTimestamp()));
            response.setSensorTimestamp(latestAlarm.getSensorTimestamp());
            response.setFormattedSensorTimestamp(formatTimestamp(latestAlarm.getSensorTimestamp()));
            response.setAlarmType(latestAlarm.getAlarmType());
            response.setAlarmThreshold(latestAlarm.getAlarmThreshold());
            response.setAlarmState(latestAlarm.getState().name());
            
            // 查询关联的点位信息
            Optional<Point> pointOptional = pointRepository.findByIdentityAndDeviceId(latestAlarm.getPointId(), latestAlarm.getDeviceId());
            if (pointOptional.isPresent()) {
                Point point = pointOptional.get();
                response.setPointId(point.getId());
                response.setPointIdentity(point.getIdentity());
                response.setPointPath(point.getPath());
                response.setPointUnit(point.getUnit());
                
                // 查询关联的设备信息
                Device device = point.getDevice();
                if (device != null) {
                    response.setDeviceId(device.getId());
                    response.setDeviceName(device.getName());
                }
            }
            
            return response;
        } catch (Exception e) {
            log.error("获取设备最新告警详情失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取设备最新告警详情失败: " + e.getMessage(), e);
        }
    }
    
    private String formatTimestamp(Long timestamp) {
        if (timestamp == null) {
            return null;
        }
        try {
            LocalDateTime dateTime = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(timestamp), java.time.ZoneId.systemDefault());
            return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            log.warn("格式化时间戳失败: {}", e.getMessage());
            return null;
        }
    }
}