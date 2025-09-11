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
        private Long alarmEndTimestamp;
        private String alarmTime;
        private String alarmEndTime;
        private String alarmValue;
        private String alarmDuration;
        private Long alarmDurationSecond;

        public AlarmListItem() {}

        public AlarmListItem(Alarm alarm) {
            this.alarmId = alarm.getId();
            this.deviceId = alarm.getDeviceId();
            this.alarmType = alarm.getAlarmType();
            this.alarmState = alarm.getState().name();
            this.pointId = alarm.getPointId();
            this.alarmThreshold = alarm.getAlarmThreshold();
            this.alarmTimestamp = alarm.getTimestamp();
            this.alarmEndTimestamp = alarm.getEndTimestamp();
            this.alarmValue = alarm.getPointValue();
            
            // 格式化时间
            if (alarm.getTimestamp() != null) {
                LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(alarm.getTimestamp()), ZoneId.systemDefault());
                this.alarmTime = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
            if (alarm.getEndTimestamp() != null) {
                LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(alarm.getEndTimestamp()), ZoneId.systemDefault());
                this.alarmEndTime = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } else {
                this.alarmEndTime = "未结束";
            }

            long duration = 0L;
            // 计算告警持续时间
            if (alarm.getEndTimestamp() != null) {
                duration = alarm.getEndTimestamp() - alarm.getTimestamp();
            } else {
                duration = System.currentTimeMillis() - alarm.getTimestamp();
            }
            duration = duration / 1000;
            this.alarmDurationSecond = duration;

            // 格式化持续时间：
            // 小于60秒显示秒
            // 大于等于60秒，小于1小时显示分秒
            // 大于等于1小时，显示小时分秒
            // 大于等于24小时，显示天小时分秒
            if (duration < 60) {
                this.alarmDuration = String.format("%d秒", duration);
            } else if (duration < 3600) {
                this.alarmDuration = String.format("%d分%d秒", duration / 60, duration % 60);
            } else if (duration < 86400) {
                this.alarmDuration = String.format("%d小时%d分%d秒", duration / 3600, (duration / 60) % 60, duration % 60);
            } else {
                this.alarmDuration = String.format("%d天%d小时%d分%d秒", duration / 86400, (duration / 3600) % 24, (duration / 60) % 60, duration % 60);
            }
        }
    }
}