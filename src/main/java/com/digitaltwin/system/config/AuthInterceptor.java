package com.digitaltwin.system.config;

import com.digitaltwin.system.entity.User;
import com.digitaltwin.system.service.UserService;
import com.digitaltwin.system.util.JwtUtil;
import com.digitaltwin.system.util.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

@Component
@Order(1)
public class AuthInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthInterceptor.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        logger.info("AuthInterceptor拦截请求: {}", requestURI);
        
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // "Bearer ".length()
            
            try {
                // 从token中解析用户ID
                Long userId = jwtUtil.getUserIdFromToken(token);
                
                // 验证token是否有效
                if (jwtUtil.validateToken(token, userId)) {
                    Optional<User> userOptional = userService.findUserById(userId);
                    if (userOptional.isPresent()) {
                        SecurityContext.setCurrentUser(userOptional.get());
                        logger.info("用户认证成功，用户ID: {}", userId);
                        return true;
                    }
                }
            } catch (Exception e) {
                logger.warn("JWT解析失败: {}", e.getMessage());
                // JWT解析失败
            }
        }

        // 对于不需要认证的接口，也允许通过
        if (requestURI.contains("/api/users/login")) {
            logger.info("白名单接口，允许访问: {}", requestURI);
            return true;
        }

        // 清除可能存在的旧用户信息
        SecurityContext.clear();
        
        // 对于需要认证但未提供有效认证信息的请求，返回401错误
        logger.info("用户未登录，拒绝访问: {}", requestURI);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"success\":false,\"message\":\"用户未登录\",\"data\":null}");
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求完成后清理SecurityContext
        SecurityContext.clear();
    }
}