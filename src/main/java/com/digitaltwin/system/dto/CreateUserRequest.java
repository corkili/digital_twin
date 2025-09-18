package com.digitaltwin.system.dto;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String username;
    private String password;
    private String email;
    private String fullName;
    private String description; // 用户描述
    private String role; // 用户角色: U=普通用户, A=管理员, SA=超级管理员
}