package com.digitaltwin.device.controller;

import com.digitaltwin.device.dto.ApiResponse;
import com.digitaltwin.device.dto.device.DeviceDto;
import com.digitaltwin.device.dto.device.CreateDeviceRequest;
import com.digitaltwin.device.dto.device.UpdateDeviceRequest;
import com.digitaltwin.device.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
public class DeviceManagementController {
    
    private final DeviceService deviceService;
    
    /**
     * 创建Device
     */
    @PostMapping
    public ResponseEntity<ApiResponse> createDevice(@RequestBody CreateDeviceRequest request) {
        try {
            com.digitaltwin.device.entity.Device device = new com.digitaltwin.device.entity.Device();
            device.setName(request.getName());
            device.setDescription(request.getDescription());
            
            DeviceDto createdDevice = deviceService.createDevice(device, request.getChannelId());
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("创建成功", createdDevice));
        } catch (Exception e) {
            log.error("创建Device失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("创建Device失败: " + e.getMessage()));
        }
    }
    
    /**
     * 根据ID获取Device
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getDeviceById(@PathVariable Long id) {
        try {
            return deviceService.getDeviceById(id)
                    .map(device -> ResponseEntity.ok(ApiResponse.success("查询成功", device)))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("未找到ID为 " + id + " 的Device")));
        } catch (Exception e) {
            log.error("查询Device失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("查询Device失败: " + e.getMessage()));
        }
    }
    
    /**
     * 根据名称获取Device
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse> getDeviceByName(@PathVariable String name) {
        try {
            return deviceService.getDeviceByName(name)
                    .map(device -> ResponseEntity.ok(ApiResponse.success("查询成功", device)))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(ApiResponse.error("未找到名称为 " + name + " 的Device")));
        } catch (Exception e) {
            log.error("查询Device失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("查询Device失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取所有Device
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getAllDevices() {
        try {
            List<DeviceDto> devices = deviceService.getAllDevices();
            return ResponseEntity.ok(ApiResponse.success("查询成功", devices));
        } catch (Exception e) {
            log.error("查询Device失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("查询Device失败: " + e.getMessage()));
        }
    }
    
    /**
     * 根据Channel ID获取Device
     */
    @GetMapping("/channel/{channelId}")
    public ResponseEntity<ApiResponse> getDevicesByChannelId(@PathVariable Long channelId) {
        try {
            List<DeviceDto> devices = deviceService.getDevicesByChannelId(channelId);
            return ResponseEntity.ok(ApiResponse.success("查询成功", devices));
        } catch (Exception e) {
            log.error("查询Device失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("查询Device失败: " + e.getMessage()));
        }
    }
    
    /**
     * 更新Device
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateDevice(@PathVariable Long id, @RequestBody UpdateDeviceRequest request) {
        try {
            com.digitaltwin.device.entity.Device device = new com.digitaltwin.device.entity.Device();
            device.setName(request.getName());
            device.setDescription(request.getDescription());
            
            DeviceDto updatedDevice = deviceService.updateDevice(id, device, request.getChannelId());
            return ResponseEntity.ok(ApiResponse.success("更新成功", updatedDevice));
        } catch (Exception e) {
            log.error("更新Device失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("更新Device失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除Device
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteDevice(@PathVariable Long id) {
        try {
            deviceService.deleteDevice(id);
            return ResponseEntity.ok(ApiResponse.success("删除成功"));
        } catch (Exception e) {
            log.error("删除Device失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("删除Device失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取设备总数
     * @return 设备总数
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse> getDeviceCount() {
        try {
            long count = deviceService.getDeviceCount();
            return ResponseEntity.ok(ApiResponse.success("设备总数查询成功", count));
        } catch (Exception e) {
            log.error("查询设备总数失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("查询设备总数失败: " + e.getMessage()));
        }
    }
}