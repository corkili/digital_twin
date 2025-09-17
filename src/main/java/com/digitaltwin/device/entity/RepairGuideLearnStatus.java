package com.digitaltwin.device.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "repair_guide_learn_status", 
       uniqueConstraints = {@UniqueConstraint(columnNames = {"guide_id", "user_id"})})
public class RepairGuideLearnStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "guide_id", nullable = false)
    private Long guideId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "is_learned", nullable = false)
    private Boolean isLearned = false;

    @Column(name = "learned_at")
    private LocalDateTime learnedAt;

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