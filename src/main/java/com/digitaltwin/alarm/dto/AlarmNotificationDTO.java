package com.digitaltwin.alarm.dto;

import lombok.Data;

@Data
public class AlarmNotificationDTO {
    private Long alarmId;
    private Long deviceId;
    private String deviceName;
    private String alarmType;
    private String pointIdentity;
}