package com.digitaltwin.system.controller;

import com.digitaltwin.device.dto.ApiResponse;
import com.digitaltwin.system.dto.*;
import com.digitaltwin.system.entity.User;
import com.digitaltwin.system.service.UserService;
import com.digitaltwin.system.util.JwtUtil;
import com.digitaltwin.system.util.SecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    

    /**
     * 获取所有用户
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getAllUsers() {
        List<UserDto> users = userService.findAll();
        return ResponseEntity.ok(ApiResponse.success("获取用户列表成功", users));
    }

    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Long id) {
        Optional<UserDto> user = userService.findById(id);
        if (user.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success("获取用户成功", user.get()));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("用户不存在"));
        }
    }

    /**
     * 创建用户
     */
    @PostMapping
    public ResponseEntity<ApiResponse> createUser(@RequestBody CreateUserRequest request) {
        try {
            UserDto user = userService.create(request);
            return ResponseEntity.ok(ApiResponse.success("创建用户成功", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateUser(@PathVariable Long id, 
                                                           @RequestBody UpdateUserRequest request) {
        try {
            UserDto user = userService.update(id, request);
            return ResponseEntity.ok(ApiResponse.success("更新用户成功", user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable Long id) {
        try {
            userService.delete(id);
            return ResponseEntity.ok(ApiResponse.success("删除用户成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 用户登录 - 本地密码验证
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@RequestBody LoginRequest request) {
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
            userDto.setCreatedAt(currentUser.getCreatedAt());
            userDto.setUpdatedAt(currentUser.getUpdatedAt());
            
            return ResponseEntity.ok(ApiResponse.success("获取当前用户信息成功", userDto));
        } else {
            // 如果没有登录用户，返回未登录错误
            return ResponseEntity.status(401).body(ApiResponse.error("用户未登录"));
        }
    }
}