package com.digitaltwin.system.service;

import com.digitaltwin.system.config.SystemConfig;
import com.digitaltwin.system.dto.UserDto;
import com.digitaltwin.system.dto.CreateUserRequest;
import com.digitaltwin.system.dto.UpdateUserRequest;
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

    /**
     * 根据外部用户信息创建或更新本地用户
     * @param username 用户名
     * @param externalUserInfo 外部用户信息
     * @return 用户实体
     */
    public User createOrUpdateUserFromExternal(String username, ExternalUserInfo externalUserInfo) {
        // 先通过UAP用户ID查找用户
        Optional<User> existingUser = userRepository.findByAuapUserId(externalUserInfo.getStaId());
        
        User user;
        if (existingUser.isPresent()) {
            // 更新现有用户
            user = existingUser.get();
        } else {
            // 检查是否有同用户名的用户
            Optional<User> userByUsername = userRepository.findByUsername(username);
            if (userByUsername.isPresent()) {
                user = userByUsername.get();
            } else {
                // 创建新用户
                user = new User();
                user.setUsername(username);
            }
        }
        
        // 更新用户信息
        user.setFullName(externalUserInfo.getStaTruename());
        user.setDeptId(externalUserInfo.getDeptId());
        user.setDeptName(externalUserInfo.getDeptName());
        user.setAuapUserId(externalUserInfo.getStaId());
        
        // 外部认证的用户不需要密码，设置一个随机值
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            user.setPassword("EXTERNAL_AUTH");
        }
        
        return userRepository.save(user);
    }

    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        BeanUtils.copyProperties(user, dto);
        return dto;
    }
}