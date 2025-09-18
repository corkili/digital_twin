package com.digitaltwin.device.dto.device;

import lombok.Data;

@Data
public class PointValueRequest {
    private Object value;
    private String identity;
}