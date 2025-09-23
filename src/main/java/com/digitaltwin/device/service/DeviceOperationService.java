package com.digitaltwin.device.service;

import com.digitaltwin.device.dto.device.DeviceOperationDto;
import com.digitaltwin.device.entity.Device;
import com.digitaltwin.device.entity.DeviceOperation;
import com.digitaltwin.device.repository.DeviceOperationRepository;
import com.digitaltwin.device.repository.DeviceRepository;
import com.digitaltwin.system.entity.User;
import com.digitaltwin.system.util.SecurityContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeviceOperationService {

    private final DeviceOperationRepository deviceOperationRepository;
    private final DeviceRepository deviceRepository;

    /**
     * 记录设备操作
     * @param deviceId 设备ID
     * @param operationType 操作类型
     * @return 操作记录DTO
     */
    @Transactional
    public DeviceOperationDto recordOperation(Long deviceId, String operationType) {
        try {
            // 验证设备是否存在
            Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
            if (!deviceOpt.isPresent()) {
                throw new RuntimeException("设备不存在，ID: " + deviceId);
            }

            Device device = deviceOpt.get();

            // 获取当前用户信息
            User currentUser = SecurityContext.getCurrentUser();

            // 创建操作记录
            DeviceOperation operation = new DeviceOperation();
            operation.setDeviceId(deviceId);
            operation.setOperationType(operationType);
            operation.setOperationTime(LocalDateTime.now());

            // 如果用户已登录，记录操作人信息
            if (currentUser != null) {
                operation.setOperatorId(currentUser.getId());
                operation.setOperatorName(currentUser.getUsername());
            }

            DeviceOperation savedOperation = deviceOperationRepository.save(operation);

            // 转换为DTO并设置设备名称
            DeviceOperationDto dto = new DeviceOperationDto(savedOperation);
            dto.setDeviceName(device.getName());

            log.info("设备操作记录创建成功: 设备ID={}, 操作类型={}, 操作人={}",
                    deviceId, operationType, currentUser != null ? currentUser.getUsername() : "未知");

            return dto;
        } catch (Exception e) {
            log.error("记录设备操作失败: {}", e.getMessage(), e);
            throw new RuntimeException("记录设备操作失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询设备操作记录
     * @param deviceId 设备ID
     * @param pageable 分页参数
     * @return 操作记录分页列表
     */
    public Page<DeviceOperationDto> getOperationsByDeviceId(Long deviceId, Pageable pageable) {
        try {
            // 验证设备是否存在
            Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
            if (!deviceOpt.isPresent()) {
                throw new RuntimeException("设备不存在，ID: " + deviceId);
            }

            Device device = deviceOpt.get();
            Page<DeviceOperation> operationsPage = deviceOperationRepository.findByDeviceIdOrderByOperationTimeDesc(deviceId, pageable);

            // 转换为DTO并设置设备名称
            List<DeviceOperationDto> dtos = operationsPage.getContent().stream()
                    .map(operation -> {
                        DeviceOperationDto dto = new DeviceOperationDto(operation);
                        dto.setDeviceName(device.getName());
                        return dto;
                    })
                    .collect(Collectors.toList());

            return new PageImpl<>(dtos, pageable, operationsPage.getTotalElements());
        } catch (Exception e) {
            log.error("查询设备操作记录失败: {}", e.getMessage(), e);
            throw new RuntimeException("查询设备操作记录失败: " + e.getMessage());
        }
    }

}