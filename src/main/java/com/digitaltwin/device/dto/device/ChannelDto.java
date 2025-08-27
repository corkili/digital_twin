package com.digitaltwin.device.dto.device;

import com.digitaltwin.device.entity.Channel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChannelDto {
    private Long id;
    private String name;
    private String serverUrl;
    private String description;
    
    public ChannelDto(Channel channel) {
        this.id = channel.getId();
        this.name = channel.getName();
        this.serverUrl = channel.getServerUrl();
        this.description = channel.getDescription();
    }
}