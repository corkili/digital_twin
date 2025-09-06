package com.digitaltwin.device.dto.device;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DevicePointCountDto {
    private Long deviceId;
    private String deviceName;
    private Long pointCount;
    
    public DevicePointCountDto(Long deviceId, String deviceName, Long pointCount) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.pointCount = pointCount;
    }
}