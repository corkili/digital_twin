package com.digitaltwin.device.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "point_failure_record")
public class PointFailureRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "point_id", nullable = false)
    private Long pointId;

    @Column(name = "failure_value")
    private String failureValue;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration")
    private Long duration;

    @Column(name = "failure_time", nullable = false)
    private LocalDateTime failureTime;

    @Column(name = "status")
    private String status; // NEW, ACTIVE, RESOLVED

    @Column(name = "description", length = 1000)
    private String description;

    @PrePersist
    protected void onCreate() {
        if (failureTime == null) {
            failureTime = LocalDateTime.now();
        }
        
        if (status == null) {
            status = "NEW";
        }
    }
}