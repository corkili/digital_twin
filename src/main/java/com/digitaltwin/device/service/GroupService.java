package com.digitaltwin.device.service;

import com.digitaltwin.device.dto.device.PointDto;
import com.digitaltwin.device.entity.Group;
import com.digitaltwin.device.entity.Point;
import com.digitaltwin.device.repository.GroupRepository;
import com.digitaltwin.device.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final PointRepository pointRepository;

    /**
     * 创建分组
     *
     * @param name 分组名称
     * @param description 分组描述
     * @return 创建的分组
     */
    public Group createGroup(String name, String description) {
        // 检查分组是否已存在
        if (groupRepository.existsByName(name)) {
            throw new RuntimeException("分组名称已存在: " + name);
        }

        Group group = new Group();
        group.setName(name);
        group.setDescription(description);
        return groupRepository.save(group);
    }

    /**
     * 根据ID获取分组
     *
     * @param id 分组ID
     * @return 分组信息
     */
    public Optional<Group> getGroupById(Long id) {
        return groupRepository.findById(id);
    }

    /**
     * 根据名称获取分组
     *
     * @param name 分组名称
     * @return 分组信息
     */
    public Optional<Group> getGroupByName(String name) {
        return groupRepository.findByName(name);
    }

    /**
     * 获取所有分组
     *
     * @return 分组列表
     */
    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    /**
     * 删除分组
     *
     * @param id 分组ID
     */
    public void deleteGroup(Long id) {
        groupRepository.deleteById(id);
    }

    /**
     * 为点位分配分组
     *
     * @param pointId 点位ID
     * @param groupId 分组ID
     * @return 更新后的点位
     */
    public Point assignPointToGroup(Long pointId, Long groupId) {
        // 检查分组是否存在
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("分组不存在，ID: " + groupId));

        // 检查点位是否存在
        Point point = pointRepository.findById(pointId)
                .orElseThrow(() -> new RuntimeException("点位不存在，ID: " + pointId));

        // 分配点位到分组
        point.setGroup(group);
        return pointRepository.save(point);
    }

    /**
     * 从分组中移除点位
     *
     * @param pointId 点位ID
     * @return 更新后的点位
     */
    public Point removePointFromGroup(Long pointId) {
        // 检查点位是否存在
        Point point = pointRepository.findById(pointId)
                .orElseThrow(() -> new RuntimeException("点位不存在，ID: " + pointId));

        // 移除点位的分组
        point.setGroup(null);
        return pointRepository.save(point);
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
        // 检查分组是否存在
        groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("分组不存在，ID: " + groupId));
        
        Page<Point> points = pointRepository.findPointsByGroupAndFilters(groupId, pointName, deviceName, pageable);
        return points.map(this::convertToDto);
    }
    
    /**
     * 将Point实体转换为PointDto
     *
     * @param point Point实体
     * @return PointDto对象
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
            dto.setDeviceName(point.getDevice().getName());
        }
        return dto;
    }
}