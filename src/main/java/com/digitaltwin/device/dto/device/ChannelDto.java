package com.digitaltwin.device.dto.device;

import com.digitaltwin.device.entity.Channel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ChannelDto {
    private Long id;
    private String name;
    private String serverUrl;
    private String description;
    private String opcUaConfig;
    
    // 审计字段
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private String updatedByName;
    private LocalDateTime updatedAt;
    
    public ChannelDto(Channel channel) {
        this.id = channel.getId();
        this.name = channel.getName();
        this.serverUrl = channel.getServerUrl();
        this.description = channel.getDescription();
        this.opcUaConfig = channel.getOpcUaConfig();
        
        // 设置审计字段
        this.createdBy = channel.getCreatedBy();
        this.createdAt = channel.getCreatedAt();
        this.updatedBy = channel.getUpdatedBy();
        this.updatedAt = channel.getUpdatedAt();
    }
}