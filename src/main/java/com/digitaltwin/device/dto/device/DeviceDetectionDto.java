package com.digitaltwin.device.dto.device;

import com.digitaltwin.device.entity.DeviceDetection;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class DeviceDetectionDto {
    private Long id;
    private Long deviceId;
    private String deviceName;
    private String parameterName;
    private BigDecimal currentValue;
    private String standardRange;
    private String status;
    private String unit;

    public DeviceDetectionDto(DeviceDetection detection) {
        this.id = detection.getId();
        this.deviceId = detection.getDeviceId();
        this.parameterName = detection.getParameterName();
        this.currentValue = detection.getCurrentValue();
        this.standardRange = detection.getStandardRange();
        this.status = detection.getStatus();
        this.unit = detection.getUnit();
    }
}