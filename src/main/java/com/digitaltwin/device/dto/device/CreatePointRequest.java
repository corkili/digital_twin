package com.digitaltwin.device.dto.device;

import com.digitaltwin.device.consts.PointPublishMethod;
import lombok.Data;

@Data
public class CreatePointRequest {
    private String identity;
    private String path;
    private Boolean writeable;
    private String unit;
    private Boolean alarmable;
    private Double upperLimit;
    private Double upperHighLimit;
    private Double lowerLimit;
    private Double lowerLowLimit;
    private PointPublishMethod publishMethod;
    private Long deviceId;
    private Double hz; // 采集频率
}