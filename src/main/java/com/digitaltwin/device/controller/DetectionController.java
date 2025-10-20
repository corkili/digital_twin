package com.digitaltwin.device.controller;

import com.digitaltwin.device.dto.ApiResponse;
import com.digitaltwin.device.dto.device.DeviceDetectionDto;
import com.digitaltwin.device.service.DeviceDetectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/detections")
@RequiredArgsConstructor
@Tag(name = "检测数据管理", description = "提供所有设备检测数据的查询接口")
public class DetectionController {

    private final DeviceDetectionService deviceDetectionService;

    /**
     * 获取所有设备检测数据列表
     */
    @Operation(summary = "获取所有设备检测数据", description = "获取系统中所有设备的检测参数数据列表")
    @GetMapping
    public ResponseEntity<ApiResponse> getAllDetections() {
        try {
            List<DeviceDetectionDto> detections = deviceDetectionService.getAllDeviceDetections();
            return ResponseEntity.ok(ApiResponse.success("查询所有设备检测数据成功", detections));
        } catch (Exception e) {
            log.error("查询所有设备检测数据失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("查询所有设备检测数据失败: " + e.getMessage()));
        }
    }
}