package com.digitaltwin.system.dto;

import lombok.Data;

/**
 * 外部认证请求DTO
 */
@Data
public class ExternalAuthRequest {
    private String userID;
    private String userPwd;
    
    public ExternalAuthRequest(String userID, String userPwd) {
        this.userID = userID;
        this.userPwd = userPwd;
    }
}