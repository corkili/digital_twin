package com.digitaltwin.websocket.controller;

import com.digitaltwin.websocket.annotation.Admin;
import com.digitaltwin.websocket.annotation.SuperAdmin;
import com.digitaltwin.websocket.model.Permission;
import com.digitaltwin.websocket.model.Role;
import com.digitaltwin.websocket.model.User;
import com.digitaltwin.websocket.model.WebSocketResponse;
import com.digitaltwin.websocket.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/**
 * 后台管理系统用户管理控制器
 * 处理用户信息、角色和权限的管理
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private static final String USER_SESSION_KEY = "currentUser";
    private final UserService userService;

    /**
     * 获取所有用户列表
     */
    @GetMapping
    @Admin
    public WebSocketResponse<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        // 移除所有用户的密码信息
        users.forEach(user -> user.setPassword(null));
        return WebSocketResponse.success(users);
    }

    /**
     * 添加用户
     */
    @PostMapping
    @Admin
    public WebSocketResponse<User> addUser(@RequestBody Map<String, Object> userInfo) {
        String username = (String) userInfo.get("username");
        String password = (String) userInfo.get("password");
        String nickname = (String) userInfo.get("nickname");
        String roleStr = (String) userInfo.get("role");
        
        Role role;
        try {
            role = Role.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的角色");
        }
        
        User user = userService.addUser(username, password, nickname, role);
        // 移除密码信息
        user.setPassword(null);
        
        return WebSocketResponse.success(user);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{username}")
    @Admin
    public WebSocketResponse<String> deleteUser(@PathVariable String username) {
        userService.deleteUser(username);
        return WebSocketResponse.success("用户删除成功");
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/{username}/reset-password")
    @Admin
    public WebSocketResponse<String> resetPassword(@PathVariable String username, @RequestBody Map<String, String> passwordInfo) {
        String newPassword = passwordInfo.get("newPassword");
        userService.resetPassword(username, newPassword);
        return WebSocketResponse.success("密码重置成功");
    }

    /**
     * 设置用户角色
     */
    @PutMapping("/{username}/role")
    @SuperAdmin
    public WebSocketResponse<User> setUserRole(@PathVariable String username, @RequestBody Map<String, String> roleInfo) {
        String roleStr = roleInfo.get("role");
        
        Role role;
        try {
            role = Role.valueOf(roleStr);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的角色");
        }
        
        User user = userService.setUserRole(username, role);
        // 移除密码信息
        user.setPassword(null);
        
        return WebSocketResponse.success(user);
    }

    /**
     * 设置用户权限
     */
    @PutMapping("/{username}/permissions")
    @Admin
    public WebSocketResponse<User> setUserPermissions(@PathVariable String username, @RequestBody Map<String, List<String>> permissionsInfo, HttpServletRequest request) {
        List<String> permissionCodes = permissionsInfo.get("permissions");
        
        // 检查当前登录用户是否有权限设置其他用户的权限
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(USER_SESSION_KEY) == null) {
            throw new SecurityException("用户未登录，请先登录");
        }
        
        User currentUser = (User) session.getAttribute(USER_SESSION_KEY);
        User targetUser = userService.getUserByUsername(username);
        
        // 管理员只能管理普通用户和自己的权限
        if (Role.ADMIN.equals(currentUser.getRole()) && Role.SUPER_ADMIN.equals(targetUser.getRole())) {
            throw new SecurityException("管理员无法管理超级管理员的权限");
        }
        
        User user = userService.setUserPermissions(username, permissionCodes);
        // 移除密码信息
        user.setPassword(null);
        
        return WebSocketResponse.success(user);
    }

    /**
     * 获取系统中所有可用的权限列表
     */
    @GetMapping("/permissions")
    @Admin
    public WebSocketResponse<List<Permission>> getAllAvailablePermissions() {
        List<Permission> permissions = userService.getAllAvailablePermissions();
        return WebSocketResponse.success(permissions);
    }

    /**
     * 检查是否可以访问后台管理系统
     */
    @GetMapping("/can-access-admin")
    public WebSocketResponse<Boolean> canAccessAdminSystem(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(USER_SESSION_KEY) == null) {
            return WebSocketResponse.success(false);
        }
        
        User currentUser = (User) session.getAttribute(USER_SESSION_KEY);
        boolean canAccess = userService.canAccessAdminSystem(currentUser);
        
        return WebSocketResponse.success(canAccess);
    }
}