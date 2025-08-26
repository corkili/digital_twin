package com.digitaltwin.websocket.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.List;

/**
 * 用户实体类
 * 标识系统中的用户
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    
    /**
     * 用户名：仅包含数字、字母和下划线，是用户的唯一标识，无法修改
     */
    @Id
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    /**
     * 登录密码：用于用户在登录后台管理系统和前台系统
     * 注意：在数据库中加密存储，不返回给前端
     */
    @JsonIgnore
    @Column(nullable = false, length = 255)
    private String password;
    
    /**
     * 昵称：用户在后台管理系统和前台系统中对外展示
     */
    @Column(nullable = false, length = 100)
    private String nickname;
    
    /**
     * 用户角色
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;
    
    /**
     * 用户拥有的权限代码列表
     * 这些权限代码会关联到permission.yml中定义的权限配置
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "user_permissions",
        joinColumns = @JoinColumn(name = "username")
    )
    @Column(name = "permission_code", nullable = false, length = 50)
    private List<String> permissionCodes;
    
    /**
     * 用户拥有的权限列表
     * 这个字段不会持久化到数据库，而是通过permissionCodes动态加载
     */
    @Transient
    private List<Permission> permissions;
}