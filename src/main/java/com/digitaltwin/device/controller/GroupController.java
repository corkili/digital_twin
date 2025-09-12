package com.digitaltwin.device.controller;

import com.digitaltwin.device.dto.ApiResponse;
import com.digitaltwin.device.dto.CreateGroupRequest;
import com.digitaltwin.device.dto.GroupDto;
import com.digitaltwin.device.dto.device.PointDto;
import com.digitaltwin.device.entity.Group;
import com.digitaltwin.device.entity.Point;
import com.digitaltwin.device.service.GroupService;
import com.digitaltwin.system.dto.UserDto;
import com.digitaltwin.system.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {
    
    private final GroupService groupService;
    private final UserService userService;
    
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
     * 更新分组信息
     *
     * @param id 分组ID
     * @param request 包含新名称和描述的请求
     * @return 更新后的分组信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateGroup(@PathVariable Long id, 
                                                  @RequestBody CreateGroupRequest request) {
        try {
            Group group = groupService.updateGroup(id, request.getName(), request.getDescription());
            GroupDto groupDto = convertToDto(group);
            return ResponseEntity.ok(ApiResponse.success("分组信息更新成功", groupDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("更新分组信息失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取所有分组
     *
     * @return 分组列表
     * @deprecated 推荐使用分页接口 /groups?page=0&size=10 以提高性能
     */
    @GetMapping
    @Deprecated
    public ResponseEntity<ApiResponse> getAllGroups() {
        List<Group> groups = groupService.getAllGroups();
        List<GroupDto> groupDtos = convertGroupsToDtos(groups);
        return ResponseEntity.ok(ApiResponse.success("查询成功", groupDtos));
    }
    
    /**
     * 分页获取分组列表
     *
     * @param page 页码（从0开始，默认为0）
     * @param size 每页大小（默认为10）
     * @param sort 排序字段（默认为id）
     * @return 分页分组列表
     */
    @GetMapping(params = {"page", "size"})
    public ResponseEntity<ApiResponse> getGroupsWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
            List<Group> groups = groupService.getAllGroups();
            
            // 手动分页
            int start = Math.min((int) pageable.getOffset(), groups.size());
            int end = Math.min(start + pageable.getPageSize(), groups.size());
            List<Group> pagedGroups = groups.subList(start, end);
            
            List<GroupDto> groupDtos = convertGroupsToDtos(pagedGroups);
            
            return ResponseEntity.ok(ApiResponse.success("查询成功", 
                    new PageImpl<>(groupDtos, pageable, groups.size())));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("查询失败: " + e.getMessage()));
        }
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
     * 根据名称模糊查询分组
     *
     * @param name 分组名称（模糊匹配）
     * @return 分组列表
     */
    @GetMapping(params = "name")
    public ResponseEntity<ApiResponse> getGroupsByNameContaining(@RequestParam String name) {
        List<Group> groups = groupService.getGroupsByNameContaining(name);
        List<GroupDto> groupDtos = groups.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("查询成功", groupDtos));
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
     * @deprecated 仅用于单个实体转换，批量转换请使用convertGroupsToDtos方法
     */
    @Deprecated
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
     * 批量将Group实体列表转换为GroupDto列表
     * 优化点：收集所有需要查询的用户ID，一次性获取所有用户信息，避免N+1查询问题
     *
     * @param groups Group实体列表
     * @return GroupDto对象列表
     */
    private List<GroupDto> convertGroupsToDtos(List<Group> groups) {
        // 收集所有需要查询的用户ID
        Set<Long> userIds = new HashSet<>();
        
        for (Group group : groups) {
            // 检查分组的points
            if (group.getPoints() != null) {
                for (Point point : group.getPoints()) {
                    if (point.getCreatedBy() != null) {
                        userIds.add(point.getCreatedBy());
                    }
                    if (point.getUpdatedBy() != null) {
                        userIds.add(point.getUpdatedBy());
                    }
                }
            }
        }
        
        // 一次性获取所有用户信息（当前没有findAllById方法，使用findAll后在内存中过滤）
        Map<Long, UserDto> userMap = new HashMap<>();
        List<UserDto> allUsers = userService.findAll();
        for (UserDto user : allUsers) {
            if (userIds.contains(user.getId())) {
                userMap.put(user.getId(), user);
            }
        }
        
        // 转换分组列表
        List<GroupDto> dtos = new ArrayList<>();
        for (Group group : groups) {
            GroupDto dto = new GroupDto();
            BeanUtils.copyProperties(group, dto);
            
            // 转换points集合
            if (group.getPoints() != null) {
                List<PointDto> pointDtos = new ArrayList<>();
                for (Point point : group.getPoints()) {
                    PointDto pointDto = new PointDto();
                    pointDto.setId(point.getId());
                    pointDto.setIdentity(point.getIdentity());
                    pointDto.setWriteable(point.getWriteable());
                    pointDto.setUnit(point.getUnit());
                    pointDto.setAlarmable(point.getAlarmable());
                    pointDto.setUpperLimit(point.getUpperLimit());
                    pointDto.setUpperHighLimit(point.getUpperHighLimit());
                    pointDto.setLowerLimit(point.getLowerLimit());
                    pointDto.setLowerLowLimit(point.getLowerLowLimit());
                    pointDto.setPublishMethod(point.getPublishMethod());
                    pointDto.setIsDefaultDisplay(point.getIsDefaultDisplay());
                    if (point.getDevice() != null) {
                        pointDto.setDeviceId(point.getDevice().getId());
                    }
                    
                    // 设置审计字段
                    pointDto.setCreatedBy(point.getCreatedBy());
                    pointDto.setCreatedAt(point.getCreatedAt());
                    pointDto.setUpdatedBy(point.getUpdatedBy());
                    pointDto.setUpdatedAt(point.getUpdatedAt());
                    
                    // 设置创建人和修改人的用户名（从缓存的用户映射中获取，避免重复查询）
                    if (point.getCreatedBy() != null && userMap.containsKey(point.getCreatedBy())) {
                        pointDto.setCreatedByName(userMap.get(point.getCreatedBy()).getUsername());
                    }
                    
                    if (point.getUpdatedBy() != null && userMap.containsKey(point.getUpdatedBy())) {
                        pointDto.setUpdatedByName(userMap.get(point.getUpdatedBy()).getUsername());
                    }
                    
                    pointDtos.add(pointDto);
                }
                dto.setPoints(pointDtos);
            }
            
            dtos.add(dto);
        }
        
        return dtos;
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
        dto.setIsDefaultDisplay(point.getIsDefaultDisplay());
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