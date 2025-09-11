package com.digitaltwin.system.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String deptId;
    private String deptName;
    private String auapUserId;
    private String role; // 用户角色
    private String description; // 用户描述
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}