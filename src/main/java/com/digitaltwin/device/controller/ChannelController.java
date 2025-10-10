package com.digitaltwin.device.controller;

import com.digitaltwin.device.dto.ApiResponse;
import com.digitaltwin.device.dto.OpcUaConfigData;
import com.digitaltwin.device.dto.channel.CreateChannelDto;
import com.digitaltwin.device.service.OpcUaConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "通道控制", description = "提供OPC UA通道的配置和管理接口，支持创建和发送OPC UA配置")
public class ChannelController {
    
    private final OpcUaConfigService opcUaConfigService;
    
    /**
     * 根据用户提供的serverUrl创建并发送OPC UA配置
     *
     * @param dto 创建通道请求数据
     * @return 发送结果
     */
    @Operation(summary = "创建并发送OPC UA配置", description = "根据用户提供的服务器URL创建默认的OPC UA配置，并发送到目标服务器")
    @PostMapping("/create-opcua-config")
    public ResponseEntity<ApiResponse> createAndSendOpcUaConfig(
            @Parameter(description = "创建通道请求数据，包含通道名称和服务器URL", required = true) @RequestBody CreateChannelDto dto) {
        try {
            // 创建默认配置
            OpcUaConfigData configData = OpcUaConfigData.createDefaultConfig(dto.getName(),dto.getServerUrl());
            
            // 更新服务器URL为用户提供的URL
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