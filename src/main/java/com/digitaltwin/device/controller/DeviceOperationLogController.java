package com.digitaltwin.device.controller;

import com.digitaltwin.device.dto.ApiResponse;
import com.digitaltwin.device.dto.device.DeviceOperationLogDto;
import com.digitaltwin.device.service.DeviceOperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/device-operation-logs")
@RequiredArgsConstructor
@Tag(name = "设备操作日志管理", description = "提供设备操作日志查询接口，支持按设备和操作员查询操作记录")
public class DeviceOperationLogController {
    
    private final DeviceOperationLogService deviceOperationLogService;
    
    /**
     * 根据设备ID获取操作日志
     */
    @Operation(summary = "根据设备ID获取操作日志", description = "查询指定设备的所有操作日志记录")
    @GetMapping("/device/{deviceId}")
    public ResponseEntity<ApiResponse> getOperationLogsByDeviceId(
            @Parameter(description = "设备ID", required = true) @PathVariable Long deviceId) {
        try {
            List<DeviceOperationLogDto> logs = deviceOperationLogService.getOperationLogsByDeviceId(deviceId);
            return ResponseEntity.ok(ApiResponse.success("查询成功", logs));
        } catch (Exception e) {
            log.error("查询设备操作日志失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error("查询设备操作日志失败: " + e.getMessage()));
        }
    }
    
    /**
     * 根据操作员ID获取操作日志
     */
    @Operation(summary = "根据操作员ID获取操作日志", description = "查询指定操作员的所有操作日志记录")
    @GetMapping("/operator/{operatorId}")
    public ResponseEntity<ApiResponse> getOperationLogsByOperatorId(
            @Parameter(description = "操作员ID", required = true) @PathVariable Long operatorId) {
        try {
            List<DeviceOperationLogDto> logs = deviceOperationLogService.getOperationLogsByOperatorId(operatorId);
            return ResponseEntity.ok(ApiResponse.success("查询成功", logs));
        } catch (Exception e) {
            log.error("查询操作员操作日志失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error("查询操作员操作日志失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取所有操作日志
     */
    @Operation(summary = "获取所有操作日志", description = "查询系统中所有设备操作日志记录")
    @GetMapping
    public ResponseEntity<ApiResponse> getAllOperationLogs() {
        try {
            List<DeviceOperationLogDto> logs = deviceOperationLogService.getAllOperationLogs();
            return ResponseEntity.ok(ApiResponse.success("查询成功", logs));
        } catch (Exception e) {
            log.error("查询所有操作日志失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(ApiResponse.error("查询所有操作日志失败: " + e.getMessage()));
        }
    }
}