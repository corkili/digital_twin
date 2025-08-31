package com.digitaltwin.device.dto.device;

import com.digitaltwin.device.consts.PointPublishMethod;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PointDto {
    private Long id;
    private String identity;
    private Boolean writeable;
    private String unit;
    private Boolean alarmable;
    private Double upperLimit;
    private Double upperHighLimit;
    private Double lowerLimit;
    private Double lowerLowLimit;
    private PointPublishMethod publishMethod;
    private Long deviceId;
    private String deviceName;
    
    // 审计字段
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private String updatedByName;
    private LocalDateTime updatedAt;
}