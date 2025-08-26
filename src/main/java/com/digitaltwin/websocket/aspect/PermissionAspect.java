package com.digitaltwin.websocket.aspect;

import com.digitaltwin.websocket.annotation.Admin;
import com.digitaltwin.websocket.annotation.PermAuth;
import com.digitaltwin.websocket.annotation.SuperAdmin;
import com.digitaltwin.websocket.model.Role;
import com.digitaltwin.websocket.model.User;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 权限切面类
 * 用于处理权限注解的校验逻辑
 */
@Slf4j
@Aspect
@Component
public class PermissionAspect {

    private static final String USER_SESSION_KEY = "currentUser";

    /**
     * 拦截带有权限注解的方法，进行权限校验
     */
    @Around("@annotation(com.digitaltwin.websocket.annotation.SuperAdmin) || @annotation(com.digitaltwin.websocket.annotation.Admin) || @annotation(com.digitaltwin.websocket.annotation.PermAuth)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取当前请求
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new SecurityException("无法获取请求上下文");
        }
        
        HttpServletRequest request = attributes.getRequest();
        HttpSession session = request.getSession(false);
        
        // 检查用户是否登录
        if (session == null || session.getAttribute(USER_SESSION_KEY) == null) {
            throw new SecurityException("用户未登录，请先登录");
        }
        
        User currentUser = (User) session.getAttribute(USER_SESSION_KEY);
        log.debug("当前登录用户: {}", currentUser.getUsername());
        
        // 获取被拦截方法的注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // 检查超级管理员权限
        if (method.isAnnotationPresent(SuperAdmin.class)) {
            if (!Role.SUPER_ADMIN.equals(currentUser.getRole())) {
                throw new SecurityException("需要超级管理员权限");
            }
        }
        
        // 检查管理员权限
        else if (method.isAnnotationPresent(Admin.class)) {
            if (!Role.SUPER_ADMIN.equals(currentUser.getRole()) && !Role.ADMIN.equals(currentUser.getRole())) {
                throw new SecurityException("需要管理员权限");
            }
        }
        
        // 检查功能权限
        else if (method.isAnnotationPresent(PermAuth.class)) {
            PermAuth permAuth = method.getAnnotation(PermAuth.class);
            String requiredPermission = permAuth.value();
            
            // 超级管理员拥有所有权限
            if (Role.SUPER_ADMIN.equals(currentUser.getRole())) {
                // 超级管理员无需进一步检查
            } else {
                List<String> userPermissionCodes = currentUser.getPermissions().stream()
                        .map(permission -> permission.getCode())
                        .collect(java.util.stream.Collectors.toList());
                
                if (!userPermissionCodes.contains(requiredPermission)) {
                    throw new SecurityException("没有权限访问此功能");
                }
            }
        }
        
        // 权限校验通过，执行原方法
        return joinPoint.proceed();
    }
}