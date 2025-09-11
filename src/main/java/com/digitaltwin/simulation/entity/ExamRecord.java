package com.digitaltwin.simulation.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "simulation_exam")
public class ExamRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 用户AUAP ID
    @Column(name = "auap_user_id", nullable = false, length = 50)
    private String auapUserId;

    // 模式：手动/自动
    @Column(nullable = false, length = 20)
    private String mode;

    // 试验名称
    @Column(nullable = false, length = 255)
    private String experimentName;

    // 试验时间
    @Column
    private LocalDateTime experimentTime;

    // 成绩
    @Column(name = "score")
    private Integer score;

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

