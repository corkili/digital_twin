package com.digitaltwin.device.dto.device;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class DeviceOperationLogDto {
    private Long id;
    private Long deviceId;
    private String deviceName;
    private String operationType;
    private Long operatorId;
    private String operatorName;
    private String description;
    private LocalDateTime createdAt;

    public DeviceOperationLogDto(com.digitaltwin.device.entity.DeviceOperationLog log) {
        this.id = log.getId();
        this.deviceId = log.getDeviceId();
        this.deviceName = log.getDeviceName();
        this.operationType = log.getOperationType();
        this.operatorId = log.getOperatorId();
        this.operatorName = log.getOperatorName();
        this.description = log.getDescription();
        this.createdAt = log.getCreatedAt();
    }
}