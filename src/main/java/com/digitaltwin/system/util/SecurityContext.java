package com.digitaltwin.system.util;

import com.digitaltwin.system.entity.User;

/**
 * 简单的安全上下文工具类，用于存储和获取当前登录用户信息
 */
public class SecurityContext {
    private static final ThreadLocal<User> currentUser = new ThreadLocal<>();

    public static void setCurrentUser(User user) {
        currentUser.set(user);
    }

    public static User getCurrentUser() {
        return currentUser.get();
    }

    public static void clear() {
        currentUser.remove();
    }
}