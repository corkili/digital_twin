package com.digitaltwin.device.dto;

import lombok.Data;

@Data
public class CreateGroupRequest {
    private String name;
    
    private String description;
}