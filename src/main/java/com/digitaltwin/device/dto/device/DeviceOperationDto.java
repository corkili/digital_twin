package com.digitaltwin.device.dto.device;

import com.digitaltwin.device.entity.DeviceOperation;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class DeviceOperationDto {
    private Long id;
    private Long deviceId;
    private String deviceName;
    private String operationType;
    private LocalDateTime operationTime;
    private Long operatorId;
    private String operatorName;

    public DeviceOperationDto(DeviceOperation operation) {
        this.id = operation.getId();
        this.deviceId = operation.getDeviceId();
        this.operationType = operation.getOperationType();
        this.operationTime = operation.getOperationTime();
        this.operatorId = operation.getOperatorId();
        this.operatorName = operation.getOperatorName();
    }
}