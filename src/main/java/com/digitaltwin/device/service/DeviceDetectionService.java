package com.digitaltwin.device.service;

import com.digitaltwin.device.dto.device.DeviceDetectionDto;
import com.digitaltwin.device.entity.Device;
import com.digitaltwin.device.entity.DeviceDetection;
import com.digitaltwin.device.repository.DeviceDetectionRepository;
import com.digitaltwin.device.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeviceDetectionService {

    private final DeviceDetectionRepository deviceDetectionRepository;
    private final DeviceRepository deviceRepository;

    /**
     * 根据设备ID获取检测数据列表
     * @param deviceId 设备ID
     * @return 检测数据列表
     */
    public List<DeviceDetectionDto> getDeviceDetectionList(Long deviceId) {
        try {
            // 验证设备是否存在
            Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
            if (!deviceOpt.isPresent()) {
                throw new RuntimeException("设备不存在，ID: " + deviceId);
            }

            Device device = deviceOpt.get();
            List<DeviceDetection> detections = deviceDetectionRepository.findByDeviceIdOrderByParameterNameAsc(deviceId);

            // 转换为DTO并设置设备名称
            return detections.stream()
                    .map(detection -> {
                        DeviceDetectionDto dto = new DeviceDetectionDto(detection);
                        dto.setDeviceName(device.getName());
                        return dto;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("查询设备检测数据失败: {}", e.getMessage(), e);
            throw new RuntimeException("查询设备检测数据失败: " + e.getMessage());
        }
    }
}