package com.digitaltwin.device.dto.channel;

import lombok.Data;

@Data
public class CreateChannelDto {
    private String name;
    private String serverUrl;
    private String description;
}
