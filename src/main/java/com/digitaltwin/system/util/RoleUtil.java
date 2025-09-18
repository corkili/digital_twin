package com.digitaltwin.system.util;

import com.digitaltwin.system.entity.User;

/**
 * 角色权限验证工具类
 */
public class RoleUtil {

    /**
     * 检查当前用户是否为超级管理员
     * @return true-是超级管理员，false-不是
     */
    public static boolean isSuperAdmin() {
        User currentUser = SecurityContext.getCurrentUser();
        return currentUser != null && "SA".equals(currentUser.getRole());
    }

    /**
     * 检查当前用户是否为管理员（包括超级管理员）
     * @return true-是管理员或超级管理员，false-不是
     */
    public static boolean isAdmin() {
        User currentUser = SecurityContext.getCurrentUser();
        return currentUser != null &&
               ("A".equals(currentUser.getRole()) || "SA".equals(currentUser.getRole()));
    }

    /**
     * 验证当前用户是否为超级管理员，如果不是则抛出异常
     * @throws RuntimeException 如果当前用户不是超级管理员
     */
    public static void requireSuperAdmin() {
        if (!isSuperAdmin()) {
            throw new RuntimeException("权限不足，只有超级管理员才能执行此操作");
        }
    }

    /**
     * 验证当前用户是否为管理员（包括超级管理员），如果不是则抛出异常
     * @throws RuntimeException 如果当前用户不是管理员
     */
    public static void requireAdmin() {
        if (!isAdmin()) {
            throw new RuntimeException("权限不足，需要管理员权限");
        }
    }

    /**
     * 获取当前用户角色
     * @return 用户角色字符串，如果未登录返回null
     */
    public static String getCurrentUserRole() {
        User currentUser = SecurityContext.getCurrentUser();
        return currentUser != null ? currentUser.getRole() : null;
    }
}