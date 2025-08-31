package com.digitaltwin.alarm.dto;

import com.digitaltwin.alarm.entity.Alarm;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
public class AlarmListResponse {
    private Long totalCount;
    private List<AlarmListItem> alarms;

    public AlarmListResponse() {}

    public AlarmListResponse(Long totalCount, List<AlarmListItem> alarms) {
        this.totalCount = totalCount;
        this.alarms = alarms;
    }

    @Data
    public static class AlarmListItem {
        private Long alarmId;
        private Long deviceId;
        private String deviceName;
        private String alarmType;
        private String alarmState;
        private String channelName;
        private String pointId;
        private String pointName;
        private String alarmThreshold;
        private Long alarmTimestamp;
        private String alarmTime;
        private String alarmValue;

        public AlarmListItem() {}

        public AlarmListItem(Alarm alarm) {
            this.alarmId = alarm.getId();
            this.deviceId = alarm.getDeviceId();
            this.alarmType = alarm.getAlarmType();
            this.alarmState = alarm.getState().name();
            this.pointId = alarm.getPointId();
            this.alarmThreshold = alarm.getAlarmThreshold();
            this.alarmTimestamp = alarm.getTimestamp();
            this.alarmValue = alarm.getPointValue();
            
            // 格式化时间
            if (alarm.getTimestamp() != null) {
                LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(alarm.getTimestamp()), ZoneId.systemDefault());
                this.alarmTime = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
        }
    }
}