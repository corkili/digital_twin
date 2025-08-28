package com.digitaltwin.device.service;

import com.digitaltwin.device.dto.OpcUaConfigData;
import com.digitaltwin.device.dto.device.CreatePointRequest;
import com.digitaltwin.device.dto.device.PointDto;
import com.digitaltwin.device.dto.device.UpdatePointRequest;
import com.digitaltwin.device.entity.Channel;
import com.digitaltwin.device.entity.Point;
import com.digitaltwin.device.repository.ChannelRepository;
import com.digitaltwin.device.repository.PointRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {

    private final static ObjectMapper ObjectMapper = new ObjectMapper();

    private final PointRepository pointRepository;
    private final ChannelRepository channelRepository;
    private final OpcUaConfigService opcUaConfigService;

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

        if (request.getChannelId() == null) {
            throw new RuntimeException("Channel not found with id: " + request.getChannelId());
        }

        Channel channel = channelRepository.findById(request.getChannelId())
                .orElseThrow(() -> new RuntimeException("Channel not found with id: " + request.getChannelId()));
        point.setChannel(channel);

        OpcUaConfigData.Timeseries timeseries = new OpcUaConfigData.Timeseries();
        timeseries.setKey(point.getIdentity());
        timeseries.setType("path");
        timeseries.setValue("${Root\\.Objects\\." + OpcUaConfigData.DeviceName + point.getPath() + "}");

        OpcUaConfigData configData = null;

        try {
            configData = ObjectMapper.readValue(channel.getOpcUaConfig(),OpcUaConfigData.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        configData.getConfigurationJson().getMapping().get(0).getTimeseries().add(timeseries);
        String result = opcUaConfigService.sendOpcUaConfig(configData);
        List<String> totalConnectorNames = channelRepository.findAll().stream()
                .map(Channel::getName)
                .collect(Collectors.toList());
        opcUaConfigService.createConnectors(totalConnectorNames);

        String opcUaConfigString = null;
        try {
            opcUaConfigString = ObjectMapper.writeValueAsString(configData);
        } catch (JsonProcessingException e) {
            log.error("ThingsBoard配置保存失败： ", e);
        }
        channel.setOpcUaConfig(opcUaConfigString);

        Channel savedChannel = channelRepository.save(channel);
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

        if (request.getChannelId() != null) {
            Channel channel = channelRepository.findById(request.getChannelId())
                    .orElseThrow(() -> new RuntimeException("Channel not found with id: " + request.getChannelId()));
            point.setChannel(channel);
        }

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
        if (point.getChannel() != null) {
            dto.setChannelId(point.getChannel().getId());
        }
        return dto;
    }
}