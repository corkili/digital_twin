package com.digitaltwin.device.dto.device;

import lombok.Data;

@Data
public class AlarmSettingRequest {
    // 点位ID，用于唯一点位设置-1
    private Long id;
    
    // 设备ID，用于唯一点位设置-2
    private Long deviceId;
    
    // 点位identity，用于所有点位设置和唯一点位设置-2
    private String identity;
    
    // 告警相关字段
    private Boolean alarmable;
    private Double upperLimit;
    private Double upperHighLimit;
    private Double lowerLimit;
    private Double lowerLowLimit;
    private Boolean stateAlarm;
}