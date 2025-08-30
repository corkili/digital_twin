package com.digitaltwin.alarm.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "alarm")
public class Alarm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "timestamp")
    private Long timestamp;
    
    @Column(name = "sensor_id")
    private String sensorId;
    
    @Column(name = "sensor_timestamp")
    private Long sensorTimestamp;
    
    @Column(name = "point_id")
    private String pointId;
    
    @Column(name = "point_value")
    private String pointValue;
    
    @Column(name = "alarm_type")
    private String alarmType;
    
    @Column(name = "alarm_threshold")
    private String alarmThreshold;
    
    @Column(name = "device_id")
    private Long deviceId;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    private AlarmState state = AlarmState.UNCONFIRMED;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (state == null) {
            state = AlarmState.UNCONFIRMED;
        }
    }
}