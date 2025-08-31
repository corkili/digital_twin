package com.digitaltwin.device.service;

import com.digitaltwin.device.entity.DeviceOperationLog;
import com.digitaltwin.device.repository.DeviceOperationLogRepository;
import com.digitaltwin.device.dto.device.DeviceOperationLogDto;
import com.digitaltwin.system.util.SecurityContext;
import com.digitaltwin.system.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceOperationLogService {
    private final DeviceOperationLogRepository deviceOperationLogRepository;

    /**
     * 记录设备操作日志
     *
     * @param deviceId     设备ID
     * @param deviceName   设备名称
     * @param operationType 操作类型 (CREATE, UPDATE, DELETE)
     * @param description  操作描述
     */
    public void logDeviceOperation(Long deviceId, String deviceName, String operationType, String description) {
        try {
            DeviceOperationLog logEntity = new DeviceOperationLog();
            logEntity.setDeviceId(deviceId);
            logEntity.setDeviceName(deviceName);
            logEntity.setOperationType(operationType);
            logEntity.setDescription(description);
            
            // 从SecurityContext获取当前用户信息
            User currentUser = SecurityContext.getCurrentUser();
            if (currentUser != null) {
                logEntity.setOperatorId(currentUser.getId());
                logEntity.setOperatorName(currentUser.getUsername());
            }
            
            deviceOperationLogRepository.save(logEntity);
            log.info("记录设备操作日志成功: 设备ID={}, 操作类型={}, 描述={}", deviceId, operationType, description);
        } catch (Exception e) {
            log.error("记录设备操作日志失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 根据设备ID获取操作日志
     *
     * @param deviceId 设备ID
     * @return 操作日志列表
     */
    public List<DeviceOperationLogDto> getOperationLogsByDeviceId(Long deviceId) {
        return deviceOperationLogRepository.findByDeviceIdOrderByCreatedAtDesc(deviceId)
                .stream()
                .map(DeviceOperationLogDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 根据操作员ID获取操作日志
     *
     * @param operatorId 操作员ID
     * @return 操作日志列表
     */
    public List<DeviceOperationLogDto> getOperationLogsByOperatorId(Long operatorId) {
        return deviceOperationLogRepository.findByOperatorIdOrderByCreatedAtDesc(operatorId)
                .stream()
                .map(DeviceOperationLogDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有操作日志
     *
     * @return 操作日志列表
     */
    public List<DeviceOperationLogDto> getAllOperationLogs() {
        return deviceOperationLogRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(DeviceOperationLogDto::new)
                .collect(Collectors.toList());
    }
}