package com.digitaltwin.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SystemConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder();
    }
    
    /**
     * 简单的密码编码器实现
     */
    public static class PasswordEncoder {
        public String encode(CharSequence rawPassword) {
            // 简单实现，实际项目中应该使用BCrypt等安全的加密算法
            return rawPassword.toString();
        }
        
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            return rawPassword.toString().equals(encodedPassword);
        }
    }
}