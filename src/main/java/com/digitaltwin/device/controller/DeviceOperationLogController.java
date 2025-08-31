package com.digitaltwin.device.controller;

import com.digitaltwin.device.dto.ApiResponse;
import com.digitaltwin.device.dto.device.DeviceOperationLogDto;
import com.digitaltwin.device.service.DeviceOperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/device-operation-logs")
@RequiredArgsConstructor
public class DeviceOperationLogController {
    
    private final DeviceOperationLogService deviceOperationLogService;
    
    /**
     * 根据设备ID获取操作日志
     */
    @GetMapping("/device/{deviceId}")
    public ResponseEntity<ApiResponse> getOperationLogsByDeviceId(@PathVariable Long deviceId) {
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
    @GetMapping("/operator/{operatorId}")
    public ResponseEntity<ApiResponse> getOperationLogsByOperatorId(@PathVariable Long operatorId) {
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