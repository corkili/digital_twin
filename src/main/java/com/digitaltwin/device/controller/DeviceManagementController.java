package com.digitaltwin.device.controller;

import com.digitaltwin.device.dto.ApiResponse;
import com.digitaltwin.device.dto.device.DeviceDto;
import com.digitaltwin.device.dto.device.CreateDeviceRequest;
import com.digitaltwin.device.dto.device.UpdateDeviceRequest;
import com.digitaltwin.device.dto.device.DeviceOperationRequest;
import com.digitaltwin.device.dto.device.DeviceOperationDto;
import com.digitaltwin.device.dto.device.DeviceDetectionDto;
import com.digitaltwin.device.service.DeviceService;
import com.digitaltwin.device.service.DeviceOperationService;
import com.digitaltwin.device.service.DeviceDetectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
@Tag(name = "设备管理", description = "提供设备的增删改查管理接口")
public class DeviceManagementController {

    private final DeviceService deviceService;
    private final DeviceOperationService deviceOperationService;
    private final DeviceDetectionService deviceDetectionService;
    
    /**
     * 创建Device
     */
    @Operation(summary = "创建设备", description = "创建新的设备信息")
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
    @Operation(summary = "根据ID获取设备", description = "根据设备ID查询设备详细信息")
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
    @Operation(summary = "根据名称获取设备", description = "根据设备名称查询设备信息")
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
     * 获取所有Device（不带分页参数时返回全部数据）
     */
    @Operation(summary = "获取所有设备", description = "获取所有设备信息列表")
    @GetMapping
    public ResponseEntity<ApiResponse> getAllDevices() {
        try {
            // 返回全部数据
            List<DeviceDto> devices = deviceService.getAllDevices();
            return ResponseEntity.ok(ApiResponse.success("查询成功", devices));
        } catch (Exception e) {
            log.error("查询Device失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("查询Device失败: " + e.getMessage()));
        }
    }
    
    /**
     * 分页获取所有Device（提供page和size参数时返回分页数据）
     * @param pageable 分页参数
     */
    @Operation(summary = "分页获取设备列表", description = "分页查询设备信息")
    @GetMapping(params = {"page", "size"})
    public ResponseEntity<ApiResponse> getAllDevicesWithPagination(Pageable pageable) {
        try {
            // 使用分页查询
            Page<DeviceDto> devicesPage = deviceService.getAllDevices(pageable);
            return ResponseEntity.ok(ApiResponse.success("分页查询成功", devicesPage));
        } catch (Exception e) {
            log.error("分页查询Device失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("分页查询Device失败: " + e.getMessage()));
        }
    }
    
    /**
     * 根据Channel ID获取Device
     */
    @Operation(summary = "根据通道ID获取设备", description = "查询指定通道下的所有设备")
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
    @Operation(summary = "更新设备", description = "根据设备ID更新设备信息")
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
     * 批量删除设备
     */
    @Operation(summary = "批量删除设备", description = "根据设备ID列表批量删除设备信息")
    @PostMapping("/batch-delete")
    public ResponseEntity<ApiResponse> batchDeleteDevices(@Valid @RequestBody List<Long> ids) {
        try {
            deviceService.deleteDevices(ids);
            return ResponseEntity.ok(ApiResponse.success("批量删除成功"));
        } catch (Exception e) {
            log.error("批量删除Device失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("批量删除Device失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取设备总数
     * @return 设备总数
     */
    @Operation(summary = "获取设备总数", description = "查询系统中设备的总数量")
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

    /**
     * 设备操作接口
     */
    @Operation(summary = "设备操作", description = "记录设备操作，支持各种操作类型如启动、停止、重启等")
    @PostMapping("/operation")
    public ResponseEntity<ApiResponse> deviceOperation(@Valid @RequestBody DeviceOperationRequest request) {
        try {
            DeviceOperationDto operationDto = deviceOperationService.recordOperation(
                    request.getDeviceId(),
                    request.getOperationType()
            );
            return ResponseEntity.ok(ApiResponse.success("设备操作记录成功", operationDto));
        } catch (Exception e) {
            log.error("设备操作失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("设备操作失败: " + e.getMessage()));
        }
    }

    /**
     * 获取设备操作记录（分页）
     */
    @Operation(summary = "获取设备操作记录", description = "分页查询指定设备的操作记录")
    @GetMapping("/{deviceId}/operations")
    public ResponseEntity<ApiResponse> getDeviceOperations(
            @PathVariable Long deviceId,
            Pageable pageable) {
        try {
            Page<DeviceOperationDto> operationsPage = deviceOperationService.getOperationsByDeviceId(deviceId, pageable);
            return ResponseEntity.ok(ApiResponse.success("查询设备操作记录成功", operationsPage));
        } catch (Exception e) {
            log.error("查询设备操作记录失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("查询设备操作记录失败: " + e.getMessage()));
        }
    }


    /**
     * 获取设备检测数据列表
     */
    @Operation(summary = "获取设备检测列表", description = "根据设备ID获取设备检测参数数据列表")
    @GetMapping("/{deviceId}/detections")
    public ResponseEntity<ApiResponse> getDeviceDetectionList(@PathVariable Long deviceId) {
        try {
            List<DeviceDetectionDto> detections = deviceDetectionService.getDeviceDetectionList(deviceId);
            return ResponseEntity.ok(ApiResponse.success("查询设备检测数据成功", detections));
        } catch (Exception e) {
            log.error("查询设备检测数据失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("查询设备检测数据失败: " + e.getMessage()));
        }
    }
}