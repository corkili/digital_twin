package com.digitaltwin.system.service;

import com.digitaltwin.system.config.SystemConfig;
import com.digitaltwin.system.dto.UserDto;
import com.digitaltwin.system.dto.CreateUserRequest;
import com.digitaltwin.system.dto.UpdateUserRequest;
import com.digitaltwin.system.dto.ChangePasswordRequest;
import com.digitaltwin.system.dto.ExternalUserInfo;
import com.digitaltwin.system.entity.User;
import com.digitaltwin.system.repository.UserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private SystemConfig.PasswordEncoder passwordEncoder;

    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<UserDto> findById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDto);
    }
    
    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<UserDto> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::convertToDto);
    }

    public UserDto create(CreateUserRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setDescription(request.getDescription()); // 设置用户描述

        // 设置用户角色，如果未提供则使用默认值U
        if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
            String role = request.getRole().trim();
            // 验证角色值是否有效
            if (!role.equals("U") && !role.equals("A") && !role.equals("SA")) {
                throw new RuntimeException("无效的角色值，只能是 U(普通用户)、A(管理员)、SA(超级管理员)");
            }
            user.setRole(role);
        } // 如果未设置role，则使用实体类的默认值U

        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    public UserDto update(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户未找到"));

        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());

        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("用户未找到");
        }
        userRepository.deleteById(id);
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        // 获取用户
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户未找到"));

        // 验证原始密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("原始密码错误");
        }

        // 验证新密码不能与原密码相同
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("新密码不能与原密码相同");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        BeanUtils.copyProperties(user, dto);
        return dto;
    }
}