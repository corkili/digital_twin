package com.digitaltwin.system.dto;

import lombok.Data;

/**
 * 外部认证响应DTO
 */
@Data
public class ExternalAuthResponse {
    private Boolean Result;         // 认证是否成功
    private ExternalUserInfo Content; // 用户信息内容
}