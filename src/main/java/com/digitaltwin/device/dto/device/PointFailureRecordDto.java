package com.digitaltwin.device.dto.device;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class PointFailureRecordDto {
    private Long id;
    private Long pointId;
    private String pointIdentity;
    private String pointName;
    private String deviceName;
    private String channelName;
    private String failureValue;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long duration;
    private LocalDateTime failureTime;
    private String description;

    public PointFailureRecordDto(com.digitaltwin.device.entity.PointFailureRecord record) {
        this.id = record.getId();
        this.pointId = record.getPointId();
        this.failureValue = record.getFailureValue();
        this.startTime = record.getStartTime();
        this.endTime = record.getEndTime();
        this.duration = record.getDuration();
        this.failureTime = record.getFailureTime();
        this.description = record.getDescription();
    }
}