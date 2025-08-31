package com.digitaltwin.alarm.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "alarm_operate_log")
public class AlarmOperateLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "operate_time")
    private LocalDateTime operateTime;
    
    @Column(name = "operate_action")
    private String operateAction;
    
    @Column(name = "alarm_id")
    private Long alarmId;
    
    @PrePersist
    protected void onCreate() {
        operateTime = LocalDateTime.now();
    }
}