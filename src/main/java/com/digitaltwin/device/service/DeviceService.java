package com.digitaltwin.device.service;

import com.digitaltwin.alarm.entity.Alarm;
import com.digitaltwin.alarm.entity.AlarmState;
import com.digitaltwin.alarm.repository.AlarmRepository;
import com.digitaltwin.device.dto.device.DeviceDto;
import com.digitaltwin.device.entity.Device;
import com.digitaltwin.device.entity.Channel;
import com.digitaltwin.device.repository.DeviceRepository;
import com.digitaltwin.device.repository.ChannelRepository;
import com.digitaltwin.device.service.DeviceOperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.digitaltwin.system.util.SecurityContext;
import com.digitaltwin.system.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {
    
    private final DeviceRepository deviceRepository;
    private final ChannelRepository channelRepository;
    private final AlarmRepository alarmRepository;
    private final DeviceOperationLogService deviceOperationLogService;
    
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
        // 设置审计创建人/修改人
        User currentUser = SecurityContext.getCurrentUser();
        if (currentUser != null) {
            device.setCreatedBy(currentUser.getId());
            device.setUpdatedBy(currentUser.getId());
        }
        
        Device savedDevice = deviceRepository.save(device);
        
        // 记录操作日志
        deviceOperationLogService.logDeviceOperation(
                savedDevice.getId(), 
                savedDevice.getName(), 
                "CREATE", 
                "创建设备: " + savedDevice.getName()
        );
        
        log.info("创建Device成功，ID: {}", savedDevice.getId());
        return convertToDeviceDtoWithStatus(savedDevice);
    }
    
    /**
     * 根据ID获取Device
     * @param id Device ID
     * @return Device实体
     */
    public Optional<DeviceDto> getDeviceById(Long id) {
        return deviceRepository.findById(id).map(this::convertToDeviceDtoWithStatus);
    }
    
    /**
     * 根据名称获取Device
     * @param name Device名称
     * @return Device实体
     */
    public Optional<DeviceDto> getDeviceByName(String name) {
        return deviceRepository.findByName(name).map(this::convertToDeviceDtoWithStatus);
    }
    
    /**
     * 获取所有Device
     * @return Device列表
     */
    public List<DeviceDto> getAllDevices() {
        return deviceRepository.findAll().stream()
                .map(this::convertToDeviceDtoWithStatus)
                .collect(Collectors.toList());
    }
    
    /**
     * 分页获取所有Device
     * @param pageable 分页参数
     * @return 分页的Device列表
     */
    public Page<DeviceDto> getAllDevices(Pageable pageable) {
        return deviceRepository.findAll(pageable)
                .map(this::convertToDeviceDtoWithStatus);
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
        
        String oldName = existingDevice.getName();
        String oldDescription = existingDevice.getDescription();
        String oldChannelName = existingDevice.getChannel() != null ? existingDevice.getChannel().getName() : null;
        
        // 检查关联的Channel是否存在
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel不存在，ID: " + channelId));
        
        // 更新Device信息
        existingDevice.setName(device.getName());
        existingDevice.setDescription(device.getDescription());
        existingDevice.setChannel(channel);
        // 设置审计修改人
        User currentUser = SecurityContext.getCurrentUser();
        if (currentUser != null) {
            existingDevice.setUpdatedBy(currentUser.getId());
        }
        
        Device updatedDevice = deviceRepository.save(existingDevice);
        
        // 记录操作日志
        StringBuilder description = new StringBuilder("更新设备详情: ");
        if (!oldName.equals(device.getName())) {
            description.append("名称从 '").append(oldName).append("' 更新为 '").append(device.getName()).append("'; ");
        }
        if (!oldDescription.equals(device.getDescription())) {
            description.append("描述从 '").append(oldDescription).append("' 更新为 '").append(device.getDescription()).append("'; ");
        }
        if (!oldChannelName.equals(channel.getName())) {
            description.append("通道从 '").append(oldChannelName).append("' 更新为 '").append(channel.getName()).append("'; ");
        }
        
        // 如果没有任何变更，则记录基本信息更新
        if (description.toString().equals("更新设备详情: ")) {
            description.append("设备基本信息更新");
        }
        
        deviceOperationLogService.logDeviceOperation(
                id, 
                updatedDevice.getName(), 
                "UPDATE", 
                description.toString()
        );
        
        log.info("更新Device成功，ID: {}", id);
        return convertToDeviceDtoWithStatus(updatedDevice);
    }
    
    /**
     * 删除Device
     * @param id Device ID
     */
    public void deleteDevice(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device不存在，ID: " + id));

        deviceRepository.deleteById(id);

        // 记录操作日志
        deviceOperationLogService.logDeviceOperation(
                id, 
                device.getName(), 
                "DELETE", 
                "删除设备: " + device.getName()
        );

        log.info("删除Device成功，ID: {}", id);
    }

    /**
     * 批量删除Device
     * @param ids Device ID列表
     */
    public void deleteDevices(List<Long> ids) {
        // 验证所有设备是否存在
        List<Device> devices = deviceRepository.findByIds(ids);
        if (devices.size() != ids.size()) {
            throw new RuntimeException("部分设备不存在");
        }
        
        deviceRepository.deleteAllById(ids);
        
        // 记录操作日志
        for (Device device : devices) {
            deviceOperationLogService.logDeviceOperation(
                    device.getId(),
                    device.getName(),
                    "DELETE",
                    "删除设备: " + device.getName()
            );
            log.info("删除Device成功，ID: {}", device.getId());
        }
    }
    
    /**
     * 根据Channel ID查找Device
     * @param channelId Channel ID
     * @return Device列表
     */
    public List<DeviceDto> getDevicesByChannelId(Long channelId) {
        return deviceRepository.findByChannelId(channelId).stream()
                .map(this::convertToDeviceDtoWithStatus)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取设备总数
     *
     * @return 设备总数
     */
    public long getDeviceCount() {
        return deviceRepository.count();
    }
    
    /**
     * 将Device实体转换为DeviceDto，并计算设备状态
     * @param device Device实体
     * @return DeviceDto对象
     */
    private DeviceDto convertToDeviceDtoWithStatus(Device device) {
        DeviceDto deviceDto = new DeviceDto(device);
        
        // 检查设备是否有未处理的告警（状态为未确认或已确认）
        boolean hasUnhandledAlarms = !alarmRepository.findByStateAndDeviceIdOrderByTimestampDesc(AlarmState.UNCONFIRMED, device.getId()).isEmpty()
                || !alarmRepository.findByStateAndDeviceIdOrderByTimestampDesc(AlarmState.CONFIRMED, device.getId()).isEmpty();

        var unhandledAlarms = alarmRepository.findByStateAndDeviceIdOrderByTimestampDesc(AlarmState.UNCONFIRMED, device.getId());
        if (hasUnhandledAlarms) {
            deviceDto.setStatus("ALARM");
            // 获取最早的告警时间作为设备告警时间
            Long alarmTime = unhandledAlarms.stream()
                    .mapToLong(Alarm::getTimestamp)
                    .min()
                    .orElse(System.currentTimeMillis());
            deviceDto.setAlarmTime(Instant.ofEpochMilli(alarmTime)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime());
        } else {
            deviceDto.setStatus("NORMAL");
        }
        
        return deviceDto;
    }
}