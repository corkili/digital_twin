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
    private String uapUserId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}