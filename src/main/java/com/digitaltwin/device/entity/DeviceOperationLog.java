package com.digitaltwin.device.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "device_operation_log")
public class DeviceOperationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    @Column(name = "device_name", nullable = false)
    private String deviceName;

    @Column(name = "operation_type", nullable = false)
    private String operationType; // CREATE, UPDATE, DELETE

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "operator_name")
    private String operatorName;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}