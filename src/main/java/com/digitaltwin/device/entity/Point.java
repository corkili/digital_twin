package com.digitaltwin.device.entity;

import com.digitaltwin.device.consts.PointPublishMethod;
import lombok.Data;
import lombok.ToString;
import javax.persistence.*;

import com.digitaltwin.device.entity.Channel;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "point")
@ToString(exclude = {"device", "group"})  // 排除device和group字段避免toString循环引用
public class Point {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private Boolean stateAlarm;
    private Double hz; // 采集频率
    
    @Enumerated(EnumType.STRING)
    private PointPublishMethod publishMethod;
    
    // 是否为默认显示项，默认为false
    @Column(name = "is_default_display", nullable = false)
    private Boolean isDefaultDisplay = false;
    
    // 是否已发布，默认为未发布(false)
    @Column(name = "published", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean published = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;
    
    // 审计字段
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 数据采集统计字段
    @Column(name = "last_collection_time")
    private LocalDateTime lastCollectionTime;
    
    @Column(name = "total_collection_duration")
    private Long totalCollectionDuration; // 总采集时长(秒)
    
    @Column(name = "total_collection_count")
    private Long totalCollectionCount; // 总采集条数
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        // 创建时，如果创建人不为空，更新人也设置为创建人
        if (createdBy != null && updatedBy == null) {
            updatedBy = createdBy;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}