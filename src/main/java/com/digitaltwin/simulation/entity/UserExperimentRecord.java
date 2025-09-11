package com.digitaltwin.simulation.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户试验记录实体类
 * 对应数据库表 simulation_user_experiment
 */
@Data
@Entity
@Table(name = "simulation_user_experiment")
public class UserExperimentRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 用户ID
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    // 基于的试验模板ID
    @Column(name = "target_experiment_id", nullable = false)
    private Long targetExperimentId;
    
    // 用户操作的步骤数据，存储为JSON
    @Column(name = "step_data", columnDefinition = "JSON")
    private String stepData;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}