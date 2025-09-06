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

    // 步骤数据 - 存储完整的步骤JSON结构
    @Column(name = "steps_data", columnDefinition = "JSON")
    private String stepsData;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}