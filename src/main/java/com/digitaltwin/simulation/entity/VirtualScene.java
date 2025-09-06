package com.digitaltwin.simulation.entity;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 虚拟场景实体类
 */
@Data
@Entity
@Table(name = "virtual_scene")
@ToString
public class VirtualScene {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "components_data", columnDefinition = "JSON")
    private String componentsData;

    @Column(name = "scenes_data", columnDefinition = "JSON")
    private String scenesData;

    @Column(length = 50)
    private String status = "ACTIVE";
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}