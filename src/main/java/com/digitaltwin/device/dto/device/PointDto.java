package com.digitaltwin.device.dto.device;

import com.digitaltwin.device.consts.PointPublishMethod;
import com.digitaltwin.device.entity.Point;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class PointDto {
    private Long id;
    private String identity;
    private String path;
    private Boolean writeable;
    private String unit;
    private Boolean alarmable;
    private Double upperLimit;
    private Double upperHighLimit;
    private Double lowerLimit;
    private Double lowerLowLimit;
    private PointPublishMethod publishMethod;
    private Long deviceId;
    private String deviceName;
    private Long groupId;
    private String groupName;
    private Double hz; // 采集频率
    
    // 添加通道相关信息
    private Long channelId;
    private String channelName;
    
    // 是否为默认显示项
    private Boolean isDefaultDisplay = false;
    
    // 审计字段
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private String updatedByName;
    private LocalDateTime updatedAt;
    
    // 数据采集统计字段
    private LocalDateTime lastCollectionTime;
    private Long totalCollectionDuration; // 总采集时长(秒)
    private Long totalCollectionCount; // 总采集条数

    public PointDto(Point point) {
        this.id = point.getId();
        this.identity = point.getIdentity();
        this.path = point.getPath();
        this.writeable = point.getWriteable();
        this.unit = point.getUnit();
        this.alarmable = point.getAlarmable();
        this.upperLimit = point.getUpperLimit();
        this.upperHighLimit = point.getUpperHighLimit();
        this.lowerLimit = point.getLowerLimit();
        this.lowerLowLimit = point.getLowerLowLimit();
        this.publishMethod = point.getPublishMethod();
        this.hz = point.getHz();
        this.isDefaultDisplay = point.getIsDefaultDisplay();
        if (point.getDevice() != null) {
            this.deviceId = point.getDevice().getId();
            this.deviceName = point.getDevice().getName();
        }
        
        // 设置分组信息
        if (point.getGroup() != null) {
            this.groupId = point.getGroup().getId();
            this.groupName = point.getGroup().getName();
        }
        
        // 设置通道信息
        if (point.getDevice() != null && point.getDevice().getChannel() != null) {
            this.channelId = point.getDevice().getChannel().getId();
            this.channelName = point.getDevice().getChannel().getName();
        }
        
        // 设置审计字段
        this.createdBy = point.getCreatedBy();
        this.createdAt = point.getCreatedAt();
        this.updatedBy = point.getUpdatedBy();
        this.updatedAt = point.getUpdatedAt();
        
        // 设置数据采集统计字段
        this.lastCollectionTime = point.getLastCollectionTime();
        this.totalCollectionDuration = point.getTotalCollectionDuration();
        this.totalCollectionCount = point.getTotalCollectionCount();
    }
}