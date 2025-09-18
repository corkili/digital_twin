package com.digitaltwin.system.controller;

import com.digitaltwin.device.dto.ApiResponse;
import com.digitaltwin.system.dto.*;
import com.digitaltwin.system.entity.User;
import com.digitaltwin.system.service.UserService;
import com.digitaltwin.system.util.JwtUtil;
import com.digitaltwin.system.util.SecurityContext;
import com.digitaltwin.system.util.RoleUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@Tag(name = "用户管理", description = "用户相关接口")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;


    /**
     * 获取所有用户
     */
    @Operation(summary = "获取所有用户", description = "获取系统中所有用户的列表")
    @GetMapping
    public ResponseEntity<ApiResponse> getAllUsers() {
        List<UserDto> users = userService.findAll();
        return ResponseEntity.ok(ApiResponse.success("获取用户列表成功", users));
    }

    /**
     * 根据ID获取用户
     */
    @Operation(summary = "根据ID获取用户", description = "通过用户ID获取用户详细信息")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getUserById(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        Optional<UserDto> user = userService.findById(id);
        if (user.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success("获取用户成功", user.get()));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("用户不存在"));
        }
    }

    /**
     * 创建用户 - 仅超级管理员可操作
     */
    @Operation(summary = "创建用户", description = "创建新用户，仅超级管理员(SA)可操作")
    @PostMapping
    public ResponseEntity<ApiResponse> createUser(
            @Parameter(description = "创建用户请求信息", required = true) @RequestBody CreateUserRequest request) {
        try {
            // 验证权限：只有超级管理员才能创建用户
            RoleUtil.requireSuperAdmin();

            UserDto user = userService.create(request);
            return ResponseEntity.ok(ApiResponse.success("创建用户成功", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }



    /**
     * 删除用户 - 仅超级管理员可操作
     */
    @Operation(summary = "删除用户", description = "删除指定用户，仅超级管理员(SA)可操作")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteUser(
            @Parameter(description = "用户ID", required = true) @PathVariable Long id) {
        try {
            // 验证权限：只有超级管理员才能删除用户
            RoleUtil.requireSuperAdmin();

            userService.delete(id);
            return ResponseEntity.ok(ApiResponse.success("删除用户成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 用户登录 - 本地密码验证
     */
    @Operation(summary = "用户登录", description = "用户名密码登录，返回JWT令牌")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(
            @Parameter(description = "登录请求信息", required = true) @RequestBody LoginRequest request) {
        // 本地用户验证
        Optional<User> userOptional = userService.findUserByUsername(request.getUsername());

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // 验证密码
            if (userService.checkPassword(request.getPassword(), user.getPassword())) {
                // 构建登录响应
                LoginResponse loginResponse = new LoginResponse();

                UserDto userDto = new UserDto();
                userDto.setId(user.getId());
                userDto.setUsername(user.getUsername());
                userDto.setEmail(user.getEmail());
                userDto.setFullName(user.getFullName());
                userDto.setDeptId(user.getDeptId());
                userDto.setDeptName(user.getDeptName());
                userDto.setRole(user.getRole()); // 设置用户角色
                userDto.setDescription(user.getDescription()); // 设置用户描述
                userDto.setCreatedAt(user.getCreatedAt());
                userDto.setUpdatedAt(user.getUpdatedAt());

                loginResponse.setUser(userDto);

                // 生成JWT token
                String token = jwtUtil.generateToken(user.getId(), user.getUsername());
                loginResponse.setToken(token);

                return ResponseEntity.ok(ApiResponse.success("登录成功", loginResponse));
            }
        }
        
        return ResponseEntity.badRequest().body(ApiResponse.error("用户名或密码错误"));
    }
    
    /**
     * 获取当前登录用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/current")
    public ResponseEntity<ApiResponse> getCurrentUser() {
        // 从SecurityContext中获取当前用户信息
        User currentUser = SecurityContext.getCurrentUser();
        if (currentUser != null) {
            UserDto userDto = new UserDto();
            userDto.setId(currentUser.getId());
            userDto.setUsername(currentUser.getUsername());
            userDto.setEmail(currentUser.getEmail());
            userDto.setFullName(currentUser.getFullName());
            userDto.setDeptId(currentUser.getDeptId());
            userDto.setDeptName(currentUser.getDeptName());
            userDto.setRole(currentUser.getRole()); // 设置用户角色
            userDto.setDescription(currentUser.getDescription()); // 设置用户描述
            userDto.setCreatedAt(currentUser.getCreatedAt());
            userDto.setUpdatedAt(currentUser.getUpdatedAt());
            
            return ResponseEntity.ok(ApiResponse.success("获取当前用户信息成功", userDto));
        } else {
            // 如果没有登录用户，返回未登录错误
            return ResponseEntity.status(401).body(ApiResponse.error("用户未登录"));
        }
    }

    /**
     * 修改当前用户密码
     */
    @Operation(summary = "修改密码", description = "修改当前登录用户的密码")
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(
            @Parameter(description = "修改密码请求信息", required = true) @RequestBody ChangePasswordRequest request) {
        try {
            // 获取当前登录用户
            User currentUser = SecurityContext.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(ApiResponse.error("用户未登录"));
            }

            // 调用服务层修改密码
            userService.changePassword(currentUser.getId(), request);
            return ResponseEntity.ok(ApiResponse.success("密码修改成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}