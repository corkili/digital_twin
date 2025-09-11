package com.digitaltwin.device.dto.device;

import com.digitaltwin.device.entity.Device;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.List;

@Data
@NoArgsConstructor
public class DeviceDto {
    private Long id;
    private String name;
    private String description;
    private ChannelDto channel;
    private List<PointDto> points;
    private String status;
    private LocalDateTime alarmTime;
    
    // 审计字段
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private String updatedByName;
    private LocalDateTime updatedAt;
    
    public DeviceDto(Device device) {
        this.id = device.getId();
        this.name = device.getName();
        this.description = device.getDescription();
        if (device.getChannel() != null) {
            this.channel = new ChannelDto(device.getChannel());
        }
        if (device.getPoints() != null) {
            this.points = device.getPoints().stream()
                    .map(PointDto::new)
                    .collect(Collectors.toList());
        }
        
        // 设置审计字段
        this.createdBy = device.getCreatedBy();
        this.createdAt = device.getCreatedAt();
        this.updatedBy = device.getUpdatedBy();
        this.updatedAt = device.getUpdatedAt();
    }
}