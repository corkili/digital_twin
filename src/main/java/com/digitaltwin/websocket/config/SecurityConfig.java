package com.digitaltwin.websocket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 安全配置类
 * 配置Spring Security相关设置
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * 配置HTTP安全策略
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF保护，因为我们使用的是RESTful API
            .csrf().disable()
            // 允许所有请求的基本认证
            .authorizeRequests()
                // 允许所有请求，权限校验由我们自定义的注解切面处理
                .anyRequest().permitAll()
            .and()
            // 配置会话管理
            .sessionManagement()
                // 使用Spring Security的会话管理，但不强制创建会话
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
    }

    /**
     * 提供密码编码器
     * 注意：当前实现中并没有实际使用此编码器，实际项目中应该使用它来加密存储密码
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}