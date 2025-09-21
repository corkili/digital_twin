package com.digitaltwin.device.service;

import com.digitaltwin.device.dto.OpcUaConfigData;
import com.digitaltwin.device.dto.device.CreatePointRequest;
import com.digitaltwin.device.dto.device.PointDto;
import com.digitaltwin.device.dto.device.PointValueRequest;
import com.digitaltwin.device.dto.device.UpdatePointRequest;
import com.digitaltwin.device.dto.device.AlarmSettingRequest;
import com.digitaltwin.device.dto.device.DevicePointCountDto;
import com.digitaltwin.device.entity.Channel;
import com.digitaltwin.device.entity.Device;
import com.digitaltwin.device.entity.Point;
import com.digitaltwin.device.repository.ChannelRepository;
import com.digitaltwin.device.repository.DeviceRepository;
import com.digitaltwin.device.repository.PointRepository;
import com.digitaltwin.system.dto.UserDto;
import com.digitaltwin.system.entity.User;
import com.digitaltwin.system.service.UserService;
import com.digitaltwin.system.util.SecurityContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
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
        point.setHz(request.getHz()); // 设置采集频率

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
//        timeseries.setType("identifier");
//        timeseries.setValue("ns=2;s="+point.getPath());
        timeseries.setType("path");
        timeseries.setValue("${Root\\.Objects\\." + OpcUaConfigData.DeviceName + point.getPath() + "}");

        OpcUaConfigData configData = null;

        try {
            configData = ObjectMapper.readValue(device.getChannel().getOpcUaConfig(),OpcUaConfigData.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        List<OpcUaConfigData.Timeseries> timeseriesList = new ArrayList<>();

        for (Device device1 : device.getChannel().getDevices()) {
            for (Point channelPoint : device1.getPoints()) {
                OpcUaConfigData.Timeseries timeseriesTemp = new OpcUaConfigData.Timeseries();
                timeseriesTemp.setKey(channelPoint.getIdentity());
                timeseriesTemp.setType("path");
//                timeseries.setValue("ns=2;s="+point.getPath());
                timeseriesTemp.setValue("${Root\\.Objects\\." + OpcUaConfigData.DeviceName + channelPoint.getPath() + "}");
                timeseriesList.add(timeseriesTemp);
            }
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
        List<Point> points = pointRepository.findByIdentity(identity);
        if (points.isEmpty()) {
            throw new RuntimeException("Point not found with identity: " + identity);
        }
        Point point = points.get(0); // 取第一个匹配的点位
        return convertToDto(point);
    }

    /**
     * 获取所有点位 - 优化版本
     *
     * @return 点位DTO列表
     */
    public List<PointDto> getAllPoints() {
        // 使用优化的查询方式，减少N+1问题
        List<Point> points = pointRepository.findAll();
        return convertPointsToDtos(points);
    }

    /**
     * 获取点位列表（分页版）- 推荐使用此接口替代getAllPoints以提高性能
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return 分页的点位DTO列表
     */
    public Page<PointDto> getPointsWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Point> pointPage = pointRepository.findAll(pageable);
        
        // 使用批量转换方法优化性能
        List<PointDto> dtos = convertPointsToDtos(pointPage.getContent());
        return new PageImpl<>(dtos, pageable, pointPage.getTotalElements());
    }
    
    /**
     * 获取点位列表（分页版带搜索）- 支持通过identity进行模糊匹配查询
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param identity 点位标识（模糊匹配）
     * @return 分页的点位DTO列表
     */
    public Page<PointDto> getPointsWithPagination(int page, int size, String identity) {
        Pageable pageable = PageRequest.of(page, size);
        // 使用原有的findAllPointsByFilters方法支持identity模糊匹配
        Page<Point> pointPage = pointRepository.findAllPointsByFilters(identity, null, pageable);
        
        // 使用批量转换方法优化性能
        List<PointDto> dtos = convertPointsToDtos(pointPage.getContent());
        return new PageImpl<>(dtos, pageable, pointPage.getTotalElements());
    }
    
    /**
     * 获取点位列表（分页版带搜索）- 支持通过identity进行模糊匹配查询和发布状态筛选
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param identity 点位标识（模糊匹配）
     * @param published 是否发布（可选，用于筛选）
     * @return 分页的点位DTO列表
     */
    public Page<PointDto> getPointsWithPagination(int page, int size, String identity, Boolean published) {
        Pageable pageable = PageRequest.of(page, size);
        // 使用新的findAllPointsByFiltersWithPublished方法支持identity模糊匹配和发布状态筛选
        Page<Point> pointPage = pointRepository.findAllPointsByFiltersWithPublished(identity, null, published, pageable);
        
        // 使用批量转换方法优化性能
        List<PointDto> dtos = convertPointsToDtos(pointPage.getContent());
        return new PageImpl<>(dtos, pageable, pointPage.getTotalElements());
    }

    /**
     * 批量转换Point实体列表为PointDto列表，优化性能
     * 
     * @param points Point实体列表
     * @return PointDto列表
     */
    private List<PointDto> convertPointsToDtos(List<Point> points) {
        if (points.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 收集所有需要查询的用户ID
        Set<Long> userIds = new HashSet<>();
        for (Point point : points) {
            if (point.getCreatedBy() != null) {
                userIds.add(point.getCreatedBy());
            }
            if (point.getUpdatedBy() != null) {
                userIds.add(point.getUpdatedBy());
            }
        }
        
        // 批量查询用户信息，避免N+1查询
        Map<Long, String> userIdToNameMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            // 由于UserService没有findAllById方法，我们使用findAll并在内存中过滤
            List<UserDto> allUsers = userService.findAll();
            for (UserDto user : allUsers) {
                if (userIds.contains(user.getId())) {
                    userIdToNameMap.put(user.getId(), user.getUsername());
                }
            }
        }
        
        // 批量转换
        List<PointDto> dtos = new ArrayList<>(points.size());
        for (Point point : points) {
            PointDto dto = new PointDto();
            dto.setId(point.getId());
            dto.setIdentity(point.getIdentity());
            dto.setPath(point.getPath());
            dto.setWriteable(point.getWriteable());
            dto.setUnit(point.getUnit());
            dto.setAlarmable(point.getAlarmable());
            dto.setUpperLimit(point.getUpperLimit());
            dto.setUpperHighLimit(point.getUpperHighLimit());
            dto.setLowerLimit(point.getLowerLimit());
            dto.setLowerLowLimit(point.getLowerLowLimit());
            dto.setPublishMethod(point.getPublishMethod());
            dto.setHz(point.getHz());
            dto.setIsDefaultDisplay(point.getIsDefaultDisplay());
            
            if (point.getDevice() != null) {
                dto.setDeviceId(point.getDevice().getId());
                dto.setDeviceName(point.getDevice().getName());
                dto.setChannelName(point.getDevice().getChannel().getName());
            }

            // 设置审计字段
            dto.setCreatedBy(point.getCreatedBy());
            dto.setCreatedAt(point.getCreatedAt());
            dto.setUpdatedBy(point.getUpdatedBy());
            dto.setUpdatedAt(point.getUpdatedAt());

            // 设置创建人和修改人的用户名（从批量查询结果中获取）
            if (point.getCreatedBy() != null) {
                dto.setCreatedByName(userIdToNameMap.get(point.getCreatedBy()));
            }

            if (point.getUpdatedBy() != null) {
                dto.setUpdatedByName(userIdToNameMap.get(point.getUpdatedBy()));
            }
            
            // 设置数据采集统计字段
            dto.setLastCollectionTime(point.getLastCollectionTime());
            dto.setTotalCollectionDuration(point.getTotalCollectionDuration());
            dto.setTotalCollectionCount(point.getTotalCollectionCount());
            
            dtos.add(dto);
        }
        
        return dtos;
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

        // 只有当字段不为null时才更新对应字段
        if (request.getIdentity() != null) {
            point.setIdentity(request.getIdentity());
        }
        if (request.getPath() != null) {
            point.setPath(request.getPath());
        }
        if (request.getWriteable() != null) {
            point.setWriteable(request.getWriteable());
        }
        if (request.getUnit() != null) {
            point.setUnit(request.getUnit());
        }
        if (request.getAlarmable() != null) {
            point.setAlarmable(request.getAlarmable());
        }
        if (request.getUpperLimit() != null) {
            point.setUpperLimit(request.getUpperLimit());
        }
        if (request.getUpperHighLimit() != null) {
            point.setUpperHighLimit(request.getUpperHighLimit());
        }
        if (request.getLowerLimit() != null) {
            point.setLowerLimit(request.getLowerLimit());
        }
        if (request.getLowerLowLimit() != null) {
            point.setLowerLowLimit(request.getLowerLowLimit());
        }
        if (request.getPublishMethod() != null) {
            point.setPublishMethod(request.getPublishMethod());
        }
        if (request.getHz() != null) {
            point.setHz(request.getHz());
        }
        if (request.getIsDefaultDisplay() != null) {
            point.setIsDefaultDisplay(request.getIsDefaultDisplay());
        }

        // 从SecurityContext获取当前用户作为修改人
        User currentUser = SecurityContext.getCurrentUser();
        if (currentUser != null) {
            point.setUpdatedBy(currentUser.getId());
        }

        Device device = point.getDevice(); // 获取点位当前关联的设备
        if (request.getDeviceId() != null) {
            device = deviceRepository.findById(request.getDeviceId())
                    .orElseThrow(() -> new RuntimeException("Device not found with id: " + request.getDeviceId()));
            point.setDevice(device);
        }

        OpcUaConfigData configData = null;

        try {
            configData = ObjectMapper.readValue(device.getChannel().getOpcUaConfig(),OpcUaConfigData.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


        List<OpcUaConfigData.Timeseries> timeseriesList = new ArrayList<>();

        for (Device device1 : device.getChannel().getDevices()) {
            for (Point channelPoint : device1.getPoints()) {
                if(channelPoint.getId().equals(point.getId())){
                    continue;
                }
                OpcUaConfigData.Timeseries timeseries = new OpcUaConfigData.Timeseries();
                timeseries.setKey(channelPoint.getIdentity());
//                timeseries.setType("path");
//                timeseries.setValue("ns=2;s="+point.getPath());
                timeseries.setType("path");
                timeseries.setValue("${Root\\.Objects\\." + OpcUaConfigData.DeviceName + channelPoint.getPath() + "}");
                timeseriesList.add(timeseries);
            }
        }

        configData.getConfigurationJson().getMapping().get(0).setTimeseries(timeseriesList);

        List<String> totalConnectorNames = channelRepository.findAll().stream()
                .map(Channel::getName)
                .collect(Collectors.toList());
        String deviceChannelName = device.getChannel().getName();
        totalConnectorNames.removeIf(x -> deviceChannelName.equals(x));
        opcUaConfigService.activeConnectors(totalConnectorNames);
        String result = opcUaConfigService.sendOpcUaConfig(configData);
        totalConnectorNames.add(deviceChannelName);
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
     * 批量删除点位
     *
     * @param ids 点位ID列表
     */
    public void deletePoints(List<Long> ids) {
        // 验证所有点位是否存在
        List<Point> points = pointRepository.findAllById(ids);
        if (points.size() != ids.size()) {
            throw new RuntimeException("部分点位不存在");
        }
        pointRepository.deleteAllById(ids);
    }

    /**
     * 设置告警
     *
     * @param request 告警设置请求
     */
    public void setAlarm(AlarmSettingRequest request) {
        // 检查是否启用告警
        if (request.getAlarmable() == null || !request.getAlarmable()) {
            // 如果未启用告警，则只更新alarmable字段
            if (request.getId() != null && request.getId() != 0) {
                // 根据点位ID设置告警
                Point point = pointRepository.findById(request.getId())
                        .orElseThrow(() -> new RuntimeException("Point not found with id: " + request.getId()));
                point.setAlarmable(false);
                pointRepository.save(point);
            } else if (request.getIdentity() != null && !request.getIdentity().isEmpty() && request.getDeviceId() != null && request.getDeviceId() != 0) {
                // 根据设备ID和点位identity设置告警
                Point point = pointRepository.findByIdentityAndDeviceId(request.getIdentity(), request.getDeviceId())
                        .orElseThrow(() -> new RuntimeException("Point not found with identity: " + request.getIdentity() + " and deviceId: " + request.getDeviceId()));
                point.setAlarmable(false);
                pointRepository.save(point);
            } else if (request.getIdentity() != null && !request.getIdentity().isEmpty()) {
                // 设置所有相应点位的告警配置
                List<Point> points = pointRepository.findByIdentity(request.getIdentity());
                if (points.isEmpty()) {
                    throw new RuntimeException("Points not found with identity: " + request.getIdentity());
                }
                for (Point point : points) {
                    point.setAlarmable(false);
                }
                pointRepository.saveAll(points);
            } else {
                throw new RuntimeException("Invalid request parameters");
            }
            return;
        }

        // 如果启用告警，则更新所有告警相关字段
        if (request.getId() != null && request.getId() != 0) {
            // 根据点位ID设置告警
            Point point = pointRepository.findById(request.getId())
                    .orElseThrow(() -> new RuntimeException("Point not found with id: " + request.getId()));
            point.setAlarmable(request.getAlarmable());
            point.setUpperLimit(request.getUpperLimit());
            point.setUpperHighLimit(request.getUpperHighLimit());
            point.setLowerLimit(request.getLowerLimit());
            point.setLowerLowLimit(request.getLowerLowLimit());
            point.setStateAlarm(request.getStateAlarm());
            pointRepository.save(point);
        } else if (request.getIdentity() != null && !request.getIdentity().isEmpty() && request.getDeviceId() != null && request.getDeviceId() != 0) {
            // 根据设备ID和点位identity设置告警
            Point point = pointRepository.findByIdentityAndDeviceId(request.getIdentity(), request.getDeviceId())
                    .orElseThrow(() -> new RuntimeException("Point not found with identity: " + request.getIdentity() + " and deviceId: " + request.getDeviceId()));
            point.setAlarmable(request.getAlarmable());
            point.setUpperLimit(request.getUpperLimit());
            point.setUpperHighLimit(request.getUpperHighLimit());
            point.setLowerLimit(request.getLowerLimit());
            point.setLowerLowLimit(request.getLowerLowLimit());
            point.setStateAlarm(request.getStateAlarm());
            pointRepository.save(point);
        } else if (request.getIdentity() != null && !request.getIdentity().isEmpty()) {
            // 设置所有相应点位的告警配置
            List<Point> points = pointRepository.findByIdentity(request.getIdentity());
            if (points.isEmpty()) {
                throw new RuntimeException("Points not found with identity: " + request.getIdentity());
            }
            for (Point point : points) {
                point.setAlarmable(request.getAlarmable());
                point.setUpperLimit(request.getUpperLimit());
                point.setUpperHighLimit(request.getUpperHighLimit());
                point.setLowerLimit(request.getLowerLimit());
                point.setLowerLowLimit(request.getLowerLowLimit());
                point.setStateAlarm(request.getStateAlarm());
            }
            pointRepository.saveAll(points);
        } else {
            throw new RuntimeException("Invalid request parameters");
        }
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
        // 注意：此方法仍保留用于单个对象转换的场景，但在批量转换时推荐使用convertPointsToDtos方法
        PointDto dto = new PointDto();
        dto.setId(point.getId());
        dto.setIdentity(point.getIdentity());
        dto.setPath(point.getPath());
        dto.setWriteable(point.getWriteable());
        dto.setUnit(point.getUnit());
        dto.setAlarmable(point.getAlarmable());
        dto.setUpperLimit(point.getUpperLimit());
        dto.setUpperHighLimit(point.getUpperHighLimit());
        dto.setLowerLimit(point.getLowerLimit());
        dto.setLowerLowLimit(point.getLowerLowLimit());
        dto.setPublishMethod(point.getPublishMethod());
        dto.setHz(point.getHz());
        dto.setIsDefaultDisplay(point.getIsDefaultDisplay());
        if (point.getDevice() != null) {
            dto.setDeviceId(point.getDevice().getId());
            dto.setDeviceName(point.getDevice().getName());
            dto.setChannelName(point.getDevice().getChannel().getName());
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
        
        // 设置数据采集统计字段
        dto.setLastCollectionTime(point.getLastCollectionTime());
        dto.setTotalCollectionDuration(point.getTotalCollectionDuration());
        dto.setTotalCollectionCount(point.getTotalCollectionCount());

        return dto;
    }

    /**
     * 根据点位ID设置点位值
     *
     * @param pointId 点位ID
     * @param request 点位值请求
     */
    public void setPointValue(Long pointId, PointValueRequest request) {
        // 查询点位信息
        Point point = pointRepository.findById(pointId)
                .orElseThrow(() -> new RuntimeException("Point not found with id: " + pointId));
        String serverUrl = point.getDevice().getChannel().getServerUrl();
        String identity = point.getIdentity();

        try {
            OpcUaClient client = OpcUaClient.create(serverUrl);
            client.connect().get();
            NodeId nodeId;
            if(StringUtils.hasText(request.getIdentity())){
                nodeId = new NodeId(1, request.getIdentity());
            }else {
                nodeId = new NodeId(1, identity);
            }
            // 2. 写入值
            DataValue value = new DataValue(new Variant(request.getValue()));
            StatusCode status = client.writeValue(nodeId, value).get();
            if(status.isBad()){
                throw new RuntimeException("写入失败: " + status);
            }
            System.out.println("写入状态: " + status);

            client.disconnect().get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (UaException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 统计每个设备内的点位数量
     * @return 设备点位统计列表
     */
    public List<DevicePointCountDto> getPointCountByDevice() {
        // 获取每个设备的点位数量统计
        List<Object[]> countResults = pointRepository.countPointsByDevice();
        
        // 获取所有相关的设备信息
        List<Long> deviceIds = countResults.stream()
                .map(result -> (Long) result[0])
                .collect(Collectors.toList());
        
        List<Device> devices = deviceRepository.findAllById(deviceIds);
        java.util.Map<Long, String> deviceIdToNameMap = devices.stream()
                .collect(Collectors.toMap(Device::getId, Device::getName));
        
        // 构造返回结果
        return countResults.stream()
                .map(result -> {
                    Long deviceId = (Long) result[0];
                    Long pointCount = (Long) result[1];
                    String deviceName = deviceIdToNameMap.getOrDefault(deviceId, "未知设备");
                    return new DevicePointCountDto(deviceId, deviceName, pointCount);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 根据发布状态统计每个设备内的点位数量
     * @param published 是否发布
     * @return 每个设备的点位数量统计结果
     */
    public List<DevicePointCountDto> getPointCountByDevice(Boolean published) {
        // 获取每个设备的点位数量统计（按发布状态筛选）
        List<Object[]> countResults = pointRepository.countPointsByDevice(published);
        
        // 获取所有相关的设备信息
        List<Long> deviceIds = countResults.stream()
                .map(result -> (Long) result[0])
                .collect(Collectors.toList());
        
        List<Device> devices = deviceRepository.findAllById(deviceIds);
        java.util.Map<Long, String> deviceIdToNameMap = devices.stream()
                .collect(Collectors.toMap(Device::getId, Device::getName));
        
        // 构造返回结果
        return countResults.stream()
                .map(result -> {
                    Long deviceId = (Long) result[0];
                    Long pointCount = (Long) result[1];
                    String deviceName = deviceIdToNameMap.getOrDefault(deviceId, "未知设备");
                    return new DevicePointCountDto(deviceId, deviceName, pointCount);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 批量更新点位的发布状态
     * @param pointIds 点位ID列表
     * @param published 发布状态
     * @return 更新后的点位DTO列表
     */
    @Transactional
    public List<PointDto> updatePointsPublishedStatus(List<Long> pointIds, Boolean published) {
        if (pointIds == null || pointIds.isEmpty()) {
            throw new RuntimeException("Point IDs cannot be empty");
        }
        
        // 获取当前用户
        User currentUser = SecurityContext.getCurrentUser();
        Long updatedById = currentUser != null ? currentUser.getId() : null;
        
        // 执行批量更新
        int updatedCount = pointRepository.updatePointsPublishedStatus(pointIds, published, updatedById);
        log.info("Updated published status for {} points", updatedCount);
        
        // 重新查询更新后的点位信息
        List<Point> points = pointRepository.findAllById(pointIds);
        
        // 返回更新后的点位DTO列表
        return convertPointsToDtos(points);
    }
}