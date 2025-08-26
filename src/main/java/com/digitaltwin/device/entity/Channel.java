package com.digitaltwin.device.entity;

import lombok.Data;

@Data
public class Channel {
    private Long id;
    private String name;
    private String serverUrl;
    private String description;
}
