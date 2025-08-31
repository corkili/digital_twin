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

    @Column(name = "failure_time", nullable = false)
    private LocalDateTime failureTime;

    @Column(name = "description", length = 1000)
    private String description;

    @PrePersist
    protected void onCreate() {
        if (failureTime == null) {
            failureTime = LocalDateTime.now();
        }
    }
}