package com.digitaltwin.system.dto;

import lombok.Data;

/**
 * 外部认证返回的用户信息
 */
@Data
public class ExternalUserInfo {
    private String staTruename; // 用户真实姓名
    private String staId;       // 用户ID
    private String deptId;      // 部门ID
    private String deptName;    // 部门名称
}