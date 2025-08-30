package com.digitaltwin.alarm.dto;

import com.digitaltwin.alarm.entity.Alarm;
import lombok.Data;

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

        public AlarmListItem() {}

        public AlarmListItem(Alarm alarm) {
            this.alarmId = alarm.getId();
            this.deviceId = alarm.getDeviceId();
            this.alarmType = alarm.getAlarmType();
            this.alarmState = alarm.getState().name();
        }
    }
}