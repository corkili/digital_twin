package com.digitaltwin.device.controller;

import com.digitaltwin.device.dto.ApiResponse;
import com.digitaltwin.device.dto.CreateGroupRequest;
import com.digitaltwin.device.dto.GroupDto;
import com.digitaltwin.device.dto.device.PointDto;
import com.digitaltwin.device.entity.Group;
import com.digitaltwin.device.entity.Point;
import com.digitaltwin.device.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {
    
    private final GroupService groupService;
    
    /**
     * 创建分组
     *
     * @param request 创建分组请求
     * @return 创建的分组信息
     */
    @PostMapping
    public ResponseEntity<ApiResponse> createGroup(@RequestBody CreateGroupRequest request) {
        try {
            Group group = groupService.createGroup(request.getName(), request.getDescription());
            GroupDto groupDto = convertToDto(group);
            return ResponseEntity.ok(ApiResponse.success("分组创建成功", groupDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("创建分组失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取所有分组
     *
     * @return 分组列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getAllGroups() {
        List<Group> groups = groupService.getAllGroups();
        List<GroupDto> groupDtos = groups.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("查询成功", groupDtos));
    }
    
    /**
     * 根据ID获取分组
     *
     * @param id 分组ID
     * @return 分组信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getGroupById(@PathVariable Long id) {
        return groupService.getGroupById(id)
                .map(group -> {
                    GroupDto groupDto = convertToDto(group);
                    return ResponseEntity.ok(ApiResponse.success("查询成功", groupDto));
                })
                .orElse(ResponseEntity.badRequest()
                        .body(ApiResponse.error("分组不存在，ID: " + id)));
    }
    
    /**
     * 根据名称获取分组
     *
     * @param name 分组名称
     * @return 分组信息
     */
    @GetMapping(params = "name")
    public ResponseEntity<ApiResponse> getGroupByName(@RequestParam String name) {
        return groupService.getGroupByName(name)
                .map(group -> {
                    GroupDto groupDto = convertToDto(group);
                    return ResponseEntity.ok(ApiResponse.success("查询成功", groupDto));
                })
                .orElse(ResponseEntity.badRequest()
                        .body(ApiResponse.error("分组不存在，名称: " + name)));
    }
    
    /**
     * 删除分组
     *
     * @param id 分组ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteGroup(@PathVariable Long id) {
        try {
            groupService.deleteGroup(id);
            return ResponseEntity.ok(ApiResponse.success("分组删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("删除分组失败: " + e.getMessage()));
        }
    }
    
    /**
     * 为点位分配分组
     *
     * @param pointId 点位ID
     * @param groupId 分组ID
     * @return 更新后的点位信息
     */
    @PostMapping("/points/{pointId}/assign/{groupId}")
    public ResponseEntity<ApiResponse> assignPointToGroup(
            @PathVariable Long pointId,
            @PathVariable Long groupId) {
        try {
            Point point = groupService.assignPointToGroup(pointId, groupId);
            Group group = point.getGroup();
            GroupDto groupDto = convertToDto(group);
            return ResponseEntity.ok(ApiResponse.success("点位分配成功", groupDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("点位分配失败: " + e.getMessage()));
        }
    }
    
    /**
     * 从分组中移除点位
     *
     * @param pointId 点位ID
     * @return 更新后的点位信息
     */
    @PostMapping("/points/{pointId}/remove")
    public ResponseEntity<ApiResponse> removePointFromGroup(@PathVariable Long pointId) {
        try {
            Point point = groupService.removePointFromGroup(pointId);
            // 如果点位原来属于某个分组，返回该分组信息
            if (point.getGroup() != null) {
                GroupDto groupDto = convertToDto(point.getGroup());
                return ResponseEntity.ok(ApiResponse.success("点位移除成功", groupDto));
            } else {
                return ResponseEntity.ok(ApiResponse.success("点位移除成功"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("点位移除失败: " + e.getMessage()));
        }
    }
    
    /**
     * 分页查询分组内的点位列表，支持通过点位名称和设备名称搜索
     *
     * @param groupId 分组ID
     * @param pointName 点位名称（可选）
     * @param deviceName 设备名称（可选）
     * @param page 页码（从0开始，默认为0）
     * @param size 每页大小（默认为10）
     * @param sort 排序字段（默认为id）
     * @return 点位分页列表
     */
    @GetMapping("/{groupId}/points")
    public ResponseEntity<ApiResponse> getPointsByGroupWithFilters(
            @PathVariable Long groupId,
            @RequestParam(required = false) String pointName,
            @RequestParam(required = false) String deviceName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort) {
        
        try {
            // 创建分页请求
            Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
            
            // 调用服务层方法获取分页数据
            Page<PointDto> points = groupService.getPointsByGroupWithFilters(groupId, pointName, deviceName, pageable);
            
            return ResponseEntity.ok(ApiResponse.success("查询成功", points));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("查询失败: " + e.getMessage()));
        }
    }
    
    /**
     * 将Group实体转换为GroupDto
     *
     * @param group Group实体
     * @return GroupDto对象
     */
    private GroupDto convertToDto(Group group) {
        GroupDto dto = new GroupDto();
        BeanUtils.copyProperties(group, dto);
        
        // 转换points集合
        if (group.getPoints() != null) {
            List<PointDto> pointDtos = group.getPoints().stream()
                    .map(this::convertPointToDto)
                    .collect(Collectors.toList());
            dto.setPoints(pointDtos);
        }
        
        return dto;
    }
    
    /**
     * 将Point实体转换为PointDto
     *
     * @param point Point实体
     * @return PointDto对象
     */
    private PointDto convertPointToDto(Point point) {
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
        return dto;
    }
}