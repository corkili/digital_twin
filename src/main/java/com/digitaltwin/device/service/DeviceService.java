package com.digitaltwin.device.service;

import com.digitaltwin.device.entity.Device;
import com.digitaltwin.device.entity.Channel;
import com.digitaltwin.device.dto.device.DeviceDto;
import com.digitaltwin.device.repository.DeviceRepository;
import com.digitaltwin.device.repository.ChannelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {
    
    private final DeviceRepository deviceRepository;
    private final ChannelRepository channelRepository;
    
    /**
     * 创建Device
     * @param device Device实体
     * @param channelId 关联的Channel ID
     * @return 保存后的Device实体
     */
    public DeviceDto createDevice(Device device, Long channelId) {
        // 检查设备名称是否已存在
        if (deviceRepository.existsByName(device.getName())) {
            throw new RuntimeException("Device名称已存在: " + device.getName());
        }
        
        // 检查关联的Channel是否存在
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel不存在，ID: " + channelId));
        
        // 设置关联关系
        device.setChannel(channel);
        
        Device savedDevice = deviceRepository.save(device);
        log.info("创建Device成功，ID: {}", savedDevice.getId());
        return new DeviceDto(savedDevice);
    }
    
    /**
     * 根据ID获取Device
     * @param id Device ID
     * @return Device实体
     */
    public Optional<DeviceDto> getDeviceById(Long id) {
        return deviceRepository.findById(id).map(DeviceDto::new);
    }
    
    /**
     * 根据名称获取Device
     * @param name Device名称
     * @return Device实体
     */
    public Optional<DeviceDto> getDeviceByName(String name) {
        return deviceRepository.findByName(name).map(DeviceDto::new);
    }
    
    /**
     * 获取所有Device
     * @return Device列表
     */
    public List<DeviceDto> getAllDevices() {
        return deviceRepository.findAll().stream()
                .map(DeviceDto::new)
                .collect(Collectors.toList());
    }
    
    /**
     * 更新Device
     * @param id Device ID
     * @param device 更新的Device信息
     * @param channelId 关联的Channel ID
     * @return 更新后的Device实体
     */
    public DeviceDto updateDevice(Long id, Device device, Long channelId) {
        // 检查Device是否存在
        Device existingDevice = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device不存在，ID: " + id));
        
        // 检查关联的Channel是否存在
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel不存在，ID: " + channelId));
        
        // 更新Device信息
        existingDevice.setName(device.getName());
        existingDevice.setDescription(device.getDescription());
        existingDevice.setChannel(channel);
        
        Device updatedDevice = deviceRepository.save(existingDevice);
        log.info("更新Device成功，ID: {}", id);
        return new DeviceDto(updatedDevice);
    }
    
    /**
     * 删除Device
     * @param id Device ID
     */
    public void deleteDevice(Long id) {
        if (!deviceRepository.existsById(id)) {
            throw new RuntimeException("Device不存在，ID: " + id);
        }
        
        deviceRepository.deleteById(id);
        log.info("删除Device成功，ID: {}", id);
    }
    
    /**
     * 根据Channel ID查找Device
     * @param channelId Channel ID
     * @return Device列表
     */
    public List<DeviceDto> getDevicesByChannelId(Long channelId) {
        return deviceRepository.findByChannelId(channelId).stream()
                .map(DeviceDto::new)
                .collect(Collectors.toList());
    }
}