package com.digitaltwin.device.entity;

import com.digitaltwin.device.consts.PointPublishMethod;
import lombok.Data;
import javax.persistence.*;

import com.digitaltwin.device.entity.Channel;

@Data
@Entity
@Table(name = "point")
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
    
    @Enumerated(EnumType.STRING)
    private PointPublishMethod publishMethod;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;
}