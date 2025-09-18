package com.digitaltwin.system.dto;

import lombok.Data;

@Data
public class ChangePasswordRequest {
    private String oldPassword; // 原始密码
    private String newPassword; // 新密码
}