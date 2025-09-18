package com.digitaltwin.simulation.entity;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "simulation_experiment")
@ToString
public class SimulationExperiment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String status = "ACTIVE";

    // 手动模式步骤数据 - 存储完整的步骤JSON结构
    @Column(name = "steps_data", columnDefinition = "JSON")
    private String stepsData;

    // 自动模式：试验流程数据
    @Column(name = "experiment_flow", columnDefinition = "JSON")
    private String experimentFlow;

    // 自动模式：应急流程数据
    @Column(name = "emergency_flow", columnDefinition = "JSON")
    private String emergencyFlow;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}