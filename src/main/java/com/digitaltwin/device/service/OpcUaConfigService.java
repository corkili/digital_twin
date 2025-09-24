package com.digitaltwin.device.service;

import com.digitaltwin.device.dto.OpcUaConfigData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OpcUaConfigService {

    private final static ObjectMapper ObjectMapper = new ObjectMapper();

    @Value("${opcua.config.gatewayId:b3238480-7db4-11f0-9b31-5b91814762c3}")
    private String gatewayId;

    @Value("${opcua.config.serverUrl:https://thingsboard.umi.xyz}")
    private String serverUrl;

    @Value("${opcua.config.username:tenant@thingsboard.org}")
    private String username;

    @Value("${opcua.config.password:Aa20250815!}")
    private String password;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public OpcUaConfigService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 登录获取访问令牌
     *
     * @return 访问令牌
     */
    private String login() {
        String loginUrl = serverUrl + "/api/auth/login";
        try {
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 创建登录请求体
            Map<String, String> loginRequest = Map.of(
                    "username", username,
                    "password", password
            );

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(loginRequest, headers);

            // 发送POST请求
            ResponseEntity<Map> response = restTemplate.postForEntity(loginUrl, requestEntity, Map.class);

            if (response.getBody() != null && response.getBody().containsKey("token")) {
                String token = (String) response.getBody().get("token");
                log.info("登录成功，获取到token");
                return token;
            } else {
                throw new RuntimeException("登录失败，响应中未包含token");
            }
        } catch (Exception e) {
            log.error("登录失败: {}", e.getMessage(), e);
            throw new RuntimeException("登录失败", e);
        }
    }

    /**
     * 创建连接器
     *
     * @param connectorNames 连接器名称列表
     * @return 接口响应结果
     */
    public String activeConnectors(List<String> connectorNames) {
        String targetUrl = serverUrl + "/api/plugins/telemetry/DEVICE/" + gatewayId + "/SHARED_SCOPE";
        try {
            // 先登录获取token
            String token = login();

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Authorization", "Bearer " + token);

            // 创建请求体
            Map<String, List<String>> requestBody = Map.of("active_connectors", connectorNames);
            HttpEntity<Map<String, List<String>>> requestEntity = new HttpEntity<>(requestBody, headers);

            // 发送POST请求
//            ResponseEntity<String> response = restTemplate.postForEntity(targetUrl, requestEntity, String.class);
//
//            log.info("连接器创建成功，状态码: {}", response.getStatusCode());
//            log.debug("响应内容: {}", response.getBody());
//
//            return response.getBody();
            return null;
        } catch (Exception e) {
            log.error("创建连接器失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建连接器失败", e);
        }
    }

    /**
     * 调用外部接口发送OPC UA配置数据
     *
     * @param opcUaData OPC UA配置数据
     * @return 接口响应结果
     */
    public String sendOpcUaConfig(OpcUaConfigData opcUaData) {
        String targetUrl = serverUrl + "/api/plugins/telemetry/DEVICE/" + gatewayId + "/SHARED_SCOPE";
        try {
            // 先登录获取token
            String token = login();

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Authorization", "Bearer " + token);

            // 创建请求体
            HttpEntity<Map<String, OpcUaConfigData>> requestEntity = new HttpEntity<>(Map.of(opcUaData.getName(), opcUaData), headers);

            log.info("发送OPC UA配置数据: {}",opcUaData);
            // 发送POST请求
//            ResponseEntity<String> response = restTemplate.postForEntity(targetUrl, requestEntity, String.class);
//
//            log.info("OPC UA配置数据发送成功，状态码: {}", response.getStatusCode());
//            log.debug("响应内容: {}", response.getBody());
//
//            return response.getBody();

            return null;

        } catch (Exception e) {
            log.error("发送OPC UA配置数据失败: {}", e.getMessage(), e);
            throw new RuntimeException("发送OPC UA配置数据失败", e);
        }
    }
}