package com.digitaltwin.device.service;

import com.digitaltwin.device.dto.OpcUaConfigData;
import com.digitaltwin.device.dto.device.CreatePointRequest;
import com.digitaltwin.device.dto.device.PointDto;
import com.digitaltwin.device.dto.device.UpdatePointRequest;
import com.digitaltwin.device.entity.Channel;
import com.digitaltwin.device.entity.Device;
import com.digitaltwin.device.entity.Point;
import com.digitaltwin.device.repository.ChannelRepository;
import com.digitaltwin.device.repository.DeviceRepository;
import com.digitaltwin.device.repository.PointRepository;
import com.digitaltwin.system.entity.User;
import com.digitaltwin.system.service.UserService;
import com.digitaltwin.system.util.SecurityContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {

    private final static ObjectMapper ObjectMapper = new ObjectMapper();

    private final PointRepository pointRepository;
    private final ChannelRepository channelRepository;
    private final DeviceRepository deviceRepository;
    private final OpcUaConfigService opcUaConfigService;
    private final UserService userService;

    /**
     * 创建点位
     *
     * @param request 创建点位请求
     * @return 点位DTO
     */
    public PointDto createPoint(CreatePointRequest request) {
        Point point = new Point();
        point.setIdentity(request.getIdentity());
        point.setPath(request.getPath());
        point.setWriteable(request.getWriteable());
        point.setUnit(request.getUnit());
        point.setAlarmable(request.getAlarmable());
        point.setUpperLimit(request.getUpperLimit());
        point.setUpperHighLimit(request.getUpperHighLimit());
        point.setLowerLimit(request.getLowerLimit());
        point.setLowerLowLimit(request.getLowerLowLimit());
        point.setPublishMethod(request.getPublishMethod());

        // 从SecurityContext获取当前用户作为创建人
        User currentUser = SecurityContext.getCurrentUser();
        if (currentUser != null) {
            point.setCreatedBy(currentUser.getId());
        }

        if (request.getDeviceId() == null) {
            throw new RuntimeException("Device not found with id: " + request.getDeviceId());
        }

//        Channel channel = channelRepository.findById(request.getChannelId())
//                .orElseThrow(() -> new RuntimeException("Channel not found with id: " + request.getChannelId()));
        Device device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new RuntimeException("Device not found with id: " + request.getDeviceId()));
        point.setDevice(device);

        OpcUaConfigData.Timeseries timeseries = new OpcUaConfigData.Timeseries();
        timeseries.setKey(point.getIdentity());
        timeseries.setType("path");
        timeseries.setValue("${Root\\.Objects\\." + OpcUaConfigData.DeviceName + point.getPath() + "}");

        OpcUaConfigData configData = null;

        try {
            configData = ObjectMapper.readValue(device.getChannel().getOpcUaConfig(),OpcUaConfigData.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        List<OpcUaConfigData.Timeseries> timeseriesList = new ArrayList<>();

        for (Point channelPoint : device.getPoints()) {
            OpcUaConfigData.Timeseries timeseriesTemp = new OpcUaConfigData.Timeseries();
            timeseriesTemp.setKey(channelPoint.getIdentity());
            timeseriesTemp.setType("path");
            timeseriesTemp.setValue("${Root\\.Objects\\." + OpcUaConfigData.DeviceName + channelPoint.getPath() + "}");
            timeseriesList.add(timeseriesTemp);
        }
        timeseriesList.add(timeseries);
        configData.getConfigurationJson().getMapping().get(0).setTimeseries(timeseriesList);

        List<String> totalConnectorNames = channelRepository.findAll().stream()
                .map(Channel::getName)
                .collect(Collectors.toList());
        totalConnectorNames.removeIf(x->device.getChannel().getName().equals(x));
        opcUaConfigService.activeConnectors(totalConnectorNames);
        String result = opcUaConfigService.sendOpcUaConfig(configData);
        totalConnectorNames.add(device.getChannel().getName());
        opcUaConfigService.activeConnectors(totalConnectorNames);

//        String opcUaConfigString = null;
//        try {
//            opcUaConfigString = ObjectMapper.writeValueAsString(configData);
//        } catch (JsonProcessingException e) {
//            log.error("ThingsBoard配置保存失败： ", e);
//        }
//        channel.setOpcUaConfig(opcUaConfigString);

        deviceRepository.save(device);
        Point savedPoint = pointRepository.save(point);
        return convertToDto(savedPoint);
    }

    /**
     * 根据ID获取点位
     *
     * @param id 点位ID
     * @return 点位DTO
     */
    public PointDto getPointById(Long id) {
        Point point = pointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Point not found with id: " + id));
        return convertToDto(point);
    }

    /**
     * 根据标识获取点位
     *
     * @param identity 点位标识
     * @return 点位DTO
     */
    public PointDto getPointByIdentity(String identity) {
        Point point = pointRepository.findByIdentity(identity)
                .orElseThrow(() -> new RuntimeException("Point not found with identity: " + identity));
        return convertToDto(point);
    }

    /**
     * 获取所有点位
     *
     * @return 点位DTO列表
     */
    public List<PointDto> getAllPoints() {
        return pointRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取点位总数
     *
     * @return 点位总数
     */
    public long getPointCount() {
        return pointRepository.count();
    }

    /**
     * 分页查询分组内的点位列表，支持通过点位名称和设备名称搜索
     *
     * @param groupId 分组ID
     * @param pointName 点位名称（可选）
     * @param deviceName 设备名称（可选）
     * @param pageable 分页参数
     * @return 点位分页列表
     */
    public Page<PointDto> getPointsByGroupWithFilters(Long groupId, String pointName, String deviceName, Pageable pageable) {
        Page<Point> points = pointRepository.findPointsByGroupAndFilters(groupId, pointName, deviceName, pageable);
        return points.map(this::convertToDto);
    }

    /**
     * 更新点位
     *
     * @param id      点位ID
     * @param request 更新点位请求
     * @return 点位DTO
     */
    public PointDto updatePoint(Long id, UpdatePointRequest request) {
        Point point = pointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Point not found with id: " + id));

        point.setIdentity(request.getIdentity());
        point.setWriteable(request.getWriteable());
        point.setUnit(request.getUnit());
        point.setAlarmable(request.getAlarmable());
        point.setUpperLimit(request.getUpperLimit());
        point.setUpperHighLimit(request.getUpperHighLimit());
        point.setLowerLimit(request.getLowerLimit());
        point.setLowerLowLimit(request.getLowerLowLimit());
        point.setPublishMethod(request.getPublishMethod());

        // 从SecurityContext获取当前用户作为修改人
        User currentUser = SecurityContext.getCurrentUser();
        if (currentUser != null) {
            point.setUpdatedBy(currentUser.getId());
        }

        if (request.getDeviceId() == null) {
            throw new RuntimeException("Device not found with id: " + request.getDeviceId());
        }

//        Channel channel = channelRepository.findById(request.getChannelId())
//                .orElseThrow(() -> new RuntimeException("Channel not found with id: " + request.getChannelId()));
        Device device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new RuntimeException("Device not found with id: " + request.getDeviceId()));
        point.setDevice(device);

        OpcUaConfigData configData = null;

        try {
            configData = ObjectMapper.readValue(device.getChannel().getOpcUaConfig(),OpcUaConfigData.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


        List<OpcUaConfigData.Timeseries> timeseriesList = new ArrayList<>();

        for (Point channelPoint : device.getPoints()) {
            if(channelPoint.getId().equals(point.getId())){
                continue;
            }
            OpcUaConfigData.Timeseries timeseries = new OpcUaConfigData.Timeseries();
            timeseries.setKey(channelPoint.getIdentity());
            timeseries.setType("path");
            timeseries.setValue("${Root\\.Objects\\." + OpcUaConfigData.DeviceName + channelPoint.getPath() + "}");
            timeseriesList.add(timeseries);
        }
        configData.getConfigurationJson().getMapping().get(0).setTimeseries(timeseriesList);

        List<String> totalConnectorNames = channelRepository.findAll().stream()
                .map(Channel::getName)
                .collect(Collectors.toList());
        totalConnectorNames.removeIf(x->device.getChannel().getName().equals(x));
        opcUaConfigService.activeConnectors(totalConnectorNames);
        String result = opcUaConfigService.sendOpcUaConfig(configData);
        totalConnectorNames.add(device.getChannel().getName());
        opcUaConfigService.activeConnectors(totalConnectorNames);

        Point updatedPoint = pointRepository.save(point);
        return convertToDto(updatedPoint);
    }

    /**
     * 删除点位
     *
     * @param id 点位ID
     */
    public void deletePoint(Long id) {
        Point point = pointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Point not found with id: " + id));
        pointRepository.delete(point);
    }

    /**
     * 根据点位ID查询其所在分组的所有点位信息
     *
     * @param pointId 点位ID
     * @return 同一分组内的所有点位列表
     */
    public List<PointDto> getPointsInSameGroup(Long pointId) {
        // 检查点位是否存在
        pointRepository.findById(pointId)
                .orElseThrow(() -> new RuntimeException("Point not found with id: " + pointId));
        
        List<Point> points = pointRepository.findPointsInSameGroup(pointId);
        return points.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 将Point实体转换为PointDto
     *
     * @param point Point实体
     * @return PointDto
     */
    private PointDto convertToDto(Point point) {
        PointDto dto = new PointDto();
        dto.setId(point.getId());
        dto.setIdentity(point.getIdentity());
        dto.setWriteable(point.getWriteable());
        dto.setUnit(point.getUnit());
        dto.setAlarmable(point.getAlarmable());
        dto.setUpperLimit(point.getUpperLimit());
        dto.setUpperHighLimit(point.getUpperHighLimit());
        dto.setLowerLimit(point.getLowerLimit());
        dto.setLowerLowLimit(point.getLowerLowLimit());
        dto.setPublishMethod(point.getPublishMethod());
        if (point.getDevice() != null) {
            dto.setDeviceId(point.getDevice().getId());
        }
        
        // 设置审计字段
        dto.setCreatedBy(point.getCreatedBy());
        dto.setCreatedAt(point.getCreatedAt());
        dto.setUpdatedBy(point.getUpdatedBy());
        dto.setUpdatedAt(point.getUpdatedAt());
        
        // 设置创建人和修改人的用户名
        if (point.getCreatedBy() != null) {
            userService.findById(point.getCreatedBy()).ifPresent(userDto -> 
                dto.setCreatedByName(userDto.getUsername())
            );
        }
        
        if (point.getUpdatedBy() != null) {
            userService.findById(point.getUpdatedBy()).ifPresent(userDto -> 
                dto.setUpdatedByName(userDto.getUsername())
            );
        }
        
        return dto;
    }
}