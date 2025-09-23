package com.digitaltwin.device.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "device_detection")
public class DeviceDetection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    @Column(name = "parameter_name", nullable = false, length = 100)
    private String parameterName;

    @Column(name = "current_value", precision = 10, scale = 3)
    private BigDecimal currentValue;

    @Column(name = "standard_range", length = 50)
    private String standardRange;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "unit", length = 20)
    private String unit;
}