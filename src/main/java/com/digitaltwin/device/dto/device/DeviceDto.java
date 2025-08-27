package com.digitaltwin.device.dto.device;

import com.digitaltwin.device.entity.Device;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DeviceDto {
    private Long id;
    private String name;
    private String description;
    private ChannelDto channel;
    
    public DeviceDto(Device device) {
        this.id = device.getId();
        this.name = device.getName();
        this.description = device.getDescription();
        if (device.getChannel() != null) {
            this.channel = new ChannelDto(device.getChannel());
        }
    }
}