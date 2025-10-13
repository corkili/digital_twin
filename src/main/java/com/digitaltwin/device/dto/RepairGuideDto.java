package com.digitaltwin.device.dto;

import lombok.Data;

@Data
public class RepairGuideDto {
    private Long id;
    private String name;
    private String type;
    private String content;
    private Long deviceId;
    private Boolean isLearned;
}