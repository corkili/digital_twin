package com.digitaltwin.device.service;

import com.digitaltwin.device.entity.Point;
import com.digitaltwin.device.repository.PointRepository;
import com.digitaltwin.device.dto.device.PointDto;
import com.digitaltwin.device.dto.device.CreatePointRequest;
import com.digitaltwin.device.dto.device.UpdatePointRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PointService {
    
    private final PointRepository pointRepository;
    
    /**
     * 创建点位
     * @param request 创建点位请求
     * @return 点位DTO
     */
    public PointDto createPoint(CreatePointRequest request) {
        Point point = new Point();
        point.setIdentity(request.getIdentity());
        point.setWriteable(request.getWriteable());
        point.setUnit(request.getUnit());
        point.setAlarmable(request.getAlarmable());
        point.setUpperLimit(request.getUpperLimit());
        point.setUpperHighLimit(request.getUpperHighLimit());
        point.setLowerLimit(request.getLowerLimit());
        point.setLowerLowLimit(request.getLowerLowLimit());
        point.setPublishMethod(request.getPublishMethod());
        
        Point savedPoint = pointRepository.save(point);
        return convertToDto(savedPoint);
    }
    
    /**
     * 根据ID获取点位
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
     * @return 点位DTO列表
     */
    public List<PointDto> getAllPoints() {
        return pointRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 更新点位
     * @param id 点位ID
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
        
        Point updatedPoint = pointRepository.save(point);
        return convertToDto(updatedPoint);
    }
    
    /**
     * 删除点位
     * @param id 点位ID
     */
    public void deletePoint(Long id) {
        Point point = pointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Point not found with id: " + id));
        pointRepository.delete(point);
    }
    
    /**
     * 将Point实体转换为PointDto
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
        return dto;
    }
}