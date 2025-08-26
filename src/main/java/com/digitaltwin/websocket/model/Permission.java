package com.digitaltwin.websocket.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 权限实体类
 * 标识用户可以访问的前台系统功能
 * 从permission.yml配置文件加载
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permission {
    
    /**
     * 权限唯一标识（英文）
     */
    private String code;
    
    /**
     * 权限名称（中文）
     */
    private String name;
    
    /**
     * 权限描述（中文）
     */
    private String description;
}