package com.digitaltwin.device.dto.device;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class DeviceOperationRequest {
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @NotBlank(message = "操作类型不能为空")
    private String operationType;
}