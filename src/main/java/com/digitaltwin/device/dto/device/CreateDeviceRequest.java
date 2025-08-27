package com.digitaltwin.device.dto.device;

import lombok.Data;

@Data
public class CreateDeviceRequest {
    private String name;
    private String description;
    private Long channelId;
}