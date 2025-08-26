package com.digitaltwin.websocket.service;

import com.digitaltwin.websocket.config.PermissionConfig;
import com.digitaltwin.websocket.model.Permission;
import com.digitaltwin.websocket.model.Role;
import com.digitaltwin.websocket.model.User;
import com.digitaltwin.websocket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 用户服务类
 * 处理用户相关的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+");
    private final UserRepository userRepository;
    private final PermissionConfig permissionConfig;
    private final PasswordEncoder passwordEncoder;

    /**
     * 初始化默认用户
     */
    @PostConstruct
    @Transactional
    public void init() {
        // 创建或更新默认超级管理员用户
        User adminUser = userRepository.findByUsername("admin");
        if (adminUser == null) {
            adminUser = new User();
            adminUser.setUsername("admin");
            // 设置为空列表，超级管理员默认拥有所有权限
            adminUser.setPermissionCodes(Collections.emptyList());
        }
        // 加密存储密码
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setNickname("超级管理员");
        adminUser.setRole(Role.SUPER_ADMIN);
        
        userRepository.save(adminUser);
        log.info("初始化超级管理员用户成功");
    }

    /**
     * 添加用户
     */
    @Transactional
    public User addUser(String username, String password, String nickname, Role role) {
        // 检查用户名格式
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException("用户名只能包含数字、字母和下划线");
        }
        
        // 检查用户是否已存在
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("用户名已存在");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // 加密存储密码
        user.setNickname(nickname);
        user.setRole(role);
        user.setPermissionCodes(Collections.emptyList());
        
        User savedUser = userRepository.save(user);
        log.info("添加用户成功: {}", username);
        return savedUser;
    }

    /**
     * 删除用户
     */
    @Transactional
    public void deleteUser(String username) {
        if (!userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("用户不存在");
        }
        
        userRepository.deleteById(username);
        log.info("删除用户成功: {}", username);
    }

    /**
     * 重置用户密码
     */
    @Transactional
    public User resetPassword(String username, String newPassword) {
        User user = getUserByUsername(username);
        user.setPassword(passwordEncoder.encode(newPassword)); // 加密存储新密码
        User updatedUser = userRepository.save(user);
        log.info("重置用户密码成功: {}", username);
        return updatedUser;
    }

    /**
     * 修改密码
     */
    @Transactional
    public User changePassword(String username, String oldPassword, String newPassword) {
        User user = getUserByUsername(username);
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("旧密码不正确");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword)); // 加密存储新密码
        User updatedUser = userRepository.save(user);
        log.info("修改用户密码成功: {}", username);
        return updatedUser;
    }

    /**
     * 设置用户角色
     */
    @Transactional
    public User setUserRole(String username, Role role) {
        User user = getUserByUsername(username);
        user.setRole(role);
        User updatedUser = userRepository.save(user);
        log.info("设置用户角色成功: {}, 角色: {}", username, role);
        return updatedUser;
    }

    /**
     * 设置用户权限
     */
    @Transactional
    public User setUserPermissions(String username, List<String> permissionCodes) {
        User user = getUserByUsername(username);
        
        // 验证权限代码是否存在
        Map<String, Permission> allPermissions = permissionConfig.allPermissions().stream()
                .collect(Collectors.toMap(Permission::getCode, p -> p));
        
        for (String code : permissionCodes) {
            if (!allPermissions.containsKey(code)) {
                throw new IllegalArgumentException("权限不存在: " + code);
            }
        }
        
        user.setPermissionCodes(permissionCodes);
        
        // 同步更新permissions列表
        List<Permission> permissions = permissionCodes.stream()
                .map(allPermissions::get)
                .collect(Collectors.toList());
        user.setPermissions(permissions);
        
        User updatedUser = userRepository.save(user);
        log.info("设置用户权限成功: {}, 权限数量: {}", username, permissionCodes.size());
        return updatedUser;
    }

    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        // 为每个用户加载权限信息
        users.forEach(this::loadUserPermissions);
        return users;
    }

    /**
     * 根据用户名获取用户
     */
    public User getUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        // 加载用户的权限信息
        loadUserPermissions(user);
        return user;
    }

    /**
     * 用户登录
     */
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username);
        
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new SecurityException("用户名或密码错误");
        }
        
        // 加载用户的权限信息
        loadUserPermissions(user);
        log.info("用户登录成功: {}", username);
        return user;
    }

    /**
     * 校验用户是否有权限访问后台管理系统
     */
    public boolean canAccessAdminSystem(User user) {
        return Role.SUPER_ADMIN.equals(user.getRole()) || Role.ADMIN.equals(user.getRole());
    }

    /**
     * 获取系统中所有可用的权限列表
     */
    public List<Permission> getAllAvailablePermissions() {
        return permissionConfig.allPermissions();
    }
    
    /**
     * 加载用户的权限信息
     * 根据permissionCodes从PermissionConfig中获取对应的Permission对象
     */
    private void loadUserPermissions(User user) {
        if (user == null) {
            return;
        }
        
        // 如果是超级管理员，拥有所有权限
        if (Role.SUPER_ADMIN.equals(user.getRole())) {
            user.setPermissions(permissionConfig.allPermissions());
            return;
        }
        
        // 根据权限代码加载权限信息
        Map<String, Permission> allPermissions = permissionConfig.allPermissions().stream()
                .collect(Collectors.toMap(Permission::getCode, p -> p));
        
        List<Permission> permissions = new ArrayList<>();
        List<String> permissionCodes = user.getPermissionCodes();
        
        if (permissionCodes != null) {
            for (String code : permissionCodes) {
                if (allPermissions.containsKey(code)) {
                    permissions.add(allPermissions.get(code));
                }
            }
        }
        
        user.setPermissions(permissions);
    }
}