package com.digitaltwin.system.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sys_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(length = 100)
    private String email;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "dept_id", length = 50)
    private String deptId;

    @Column(name = "dept_name", length = 100)
    private String deptName;

    @Column(name = "auap_user_id", length = 50)
    private String auapUserId;
    
    @Column(name = "role", length = 50)
    private String role = "U"; // 默认角色为普通用户(U=普通用户, A=管理员, SA=超级管理员)

    @Column(name = "description", length = 500)
    private String description; // 用户描述

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