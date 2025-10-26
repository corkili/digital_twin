package com.digitaltwin.device.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "fault_category")
public class FaultCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fault_type", nullable = false, length = 100)
    private String faultType;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "training_difficulty", nullable = false)
    private Integer trainingDifficulty;

    @Column(name = "ue")
    private String ue;
}