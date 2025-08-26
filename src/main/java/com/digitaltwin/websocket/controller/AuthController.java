package com.digitaltwin.websocket.controller;

import com.digitaltwin.websocket.model.User;
import com.digitaltwin.websocket.service.UserService;
import com.digitaltwin.websocket.model.WebSocketResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * 用户认证控制器
 * 处理前台系统的用户登录、注销和修改密码等功能
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String USER_SESSION_KEY = "currentUser";
    private final UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public WebSocketResponse<User> login(@RequestBody Map<String, String> loginInfo, HttpServletRequest request) {
        String username = loginInfo.get("username");
        String password = loginInfo.get("password");
        
        log.info("用户登录请求: {}", username);
        User user = userService.login(username, password);
        
        // 登录成功，将用户信息存入session
        HttpSession session = request.getSession(true);
        session.setAttribute(USER_SESSION_KEY, user);
        
        // 移除密码信息，避免泄露
        user.setPassword(null);
        
        return WebSocketResponse.success(user);
    }

    /**
     * 用户注销
     */
    @PostMapping("/logout")
    public WebSocketResponse<String> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            User user = (User) session.getAttribute(USER_SESSION_KEY);
            if (user != null) {
                log.info("用户注销: {}", user.getUsername());
            }
            session.invalidate();
        }
        
        return WebSocketResponse.success("注销成功");
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public WebSocketResponse<String> changePassword(@RequestBody Map<String, String> passwordInfo, HttpServletRequest request) {
        String oldPassword = passwordInfo.get("oldPassword");
        String newPassword = passwordInfo.get("newPassword");
        
        // 检查用户是否登录
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(USER_SESSION_KEY) == null) {
            throw new SecurityException("用户未登录，请先登录");
        }
        
        User currentUser = (User) session.getAttribute(USER_SESSION_KEY);
        userService.changePassword(currentUser.getUsername(), oldPassword, newPassword);
        
        // 修改密码后需要重新登录
        session.invalidate();
        
        return WebSocketResponse.success("密码修改成功，请重新登录");
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/current-user")
    public WebSocketResponse<User> getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(USER_SESSION_KEY) == null) {
            throw new SecurityException("用户未登录，请先登录");
        }
        
        User currentUser = (User) session.getAttribute(USER_SESSION_KEY);
        // 移除密码信息，避免泄露
        currentUser.setPassword(null);
        
        return WebSocketResponse.success(currentUser);
    }

    /**
     * 检查用户是否登录
     */
    @GetMapping("/is-logged-in")
    public WebSocketResponse<Boolean> isLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        boolean isLoggedIn = session != null && session.getAttribute(USER_SESSION_KEY) != null;
        
        return WebSocketResponse.success(isLoggedIn);
    }
}