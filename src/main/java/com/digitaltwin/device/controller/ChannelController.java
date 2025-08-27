package com.digitaltwin.device.controller;

import com.digitaltwin.device.dto.ApiResponse;
import com.digitaltwin.device.dto.OpcUaConfigData;
import com.digitaltwin.device.dto.channel.CreateChannelDto;
import com.digitaltwin.device.service.OpcUaConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/channel")
@RequiredArgsConstructor
public class ChannelController {
    
    private final OpcUaConfigService opcUaConfigService;
    
    /**
     * 根据用户提供的serverUrl创建并发送OPC UA配置
     * 
     * @return 发送结果
     */
    @PostMapping("/create-opcua-config")
    public ResponseEntity<ApiResponse> createAndSendOpcUaConfig(@RequestBody CreateChannelDto dto) {
        try {
            // 创建默认配置
            OpcUaConfigData configData = OpcUaConfigData.createDefaultConfig(dto.getName());
            
            // 更新服务器URL为用户提供的URL
            configData.getConfigurationJson().getServer().setUrl(dto.getServerUrl());
            configData.setName(dto.getName());
            
            // 发送配置到目标URL
            String result = opcUaConfigService.sendOpcUaConfig(configData);
            
            return ResponseEntity.ok(ApiResponse.success("配置已成功发送", result));
            
        } catch (Exception e) {
            log.error("创建并发送OPC UA配置失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("发送失败: " + e.getMessage()));
        }
    }
}