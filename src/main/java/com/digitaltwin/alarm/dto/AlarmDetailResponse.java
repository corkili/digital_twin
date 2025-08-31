package com.digitaltwin.alarm.dto;

import com.digitaltwin.alarm.entity.AlarmOperateLog;
import lombok.Data;

import java.util.List;

@Data
public class AlarmDetailResponse {
    // 告警信息
    private Long alarmId;
    private Long timestamp;
    private String formattedTimestamp;
    private Long sensorTimestamp;
    private String formattedSensorTimestamp;
    private String alarmType;
    private String alarmThreshold;
    private String alarmState;
    
    // 关联的点位信息
    private Long pointId;
    private String pointIdentity;
    private String pointPath;
    private String pointUnit;
    
    // 关联的设备信息
    private Long deviceId;
    private String deviceName;
    
    // 操作日志列表
    private List<AlarmOperateLog> operateLogs;
}