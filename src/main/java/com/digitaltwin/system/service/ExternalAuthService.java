package com.digitaltwin.system.service;

import com.digitaltwin.system.dto.ExternalAuthResponse;
import com.digitaltwin.system.dto.ExternalUserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 外部认证服务
 */
@Service
public class ExternalAuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(ExternalAuthService.class);
    
    @Value("${external.auth.url:}")
    private String externalAuthUrl;
    
    @Autowired
    private RestTemplate restTemplate;
    
    /**
     * 调用外部认证接口验证用户
     * @param username 用户名
     * @param password 密码
     * @return 认证成功返回用户信息，失败返回null
     */
    public ExternalUserInfo authenticate(String username, String password) {
        try {
            if (externalAuthUrl == null || externalAuthUrl.trim().isEmpty()) {
                logger.error("外部认证接口地址未配置");
                return null;
            }
            
            // 构建请求URL
            String url = UriComponentsBuilder.fromHttpUrl(externalAuthUrl)
                    .queryParam("userID", username)
                    .queryParam("userPwd", password)
                    .build()
                    .toUriString();
            
            logger.info("调用外部认证接口: {}", url.replaceAll("userPwd=[^&]*", "userPwd=***"));
            
            // 发送GET请求
            ResponseEntity<ExternalAuthResponse> response = restTemplate.getForEntity(
                url, ExternalAuthResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ExternalAuthResponse authResponse = response.getBody();
                
                if (Boolean.TRUE.equals(authResponse.getResult()) && authResponse.getContent() != null) {
                    logger.info("外部认证成功，用户: {}", username);
                    return authResponse.getContent();
                } else {
                    logger.warn("外部认证失败，用户: {}", username);
                    return null;
                }
            } else {
                logger.error("外部认证接口调用失败，状态码: {}", response.getStatusCode());
                return null;
            }
            
        } catch (Exception e) {
            logger.error("调用外部认证接口异常: {}", e.getMessage(), e);
            return null;
        }
    }
}