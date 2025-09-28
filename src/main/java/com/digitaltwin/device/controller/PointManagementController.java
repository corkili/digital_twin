package com.digitaltwin.device.controller;

import com.digitaltwin.device.dto.ApiResponse;
import com.digitaltwin.device.dto.device.AlarmSettingRequest;
import com.digitaltwin.device.dto.device.BatchUpdatePointsStatusRequest;
import com.digitaltwin.device.dto.device.CreatePointRequest;
import com.digitaltwin.device.dto.device.DevicePointCountDto;
import com.digitaltwin.device.dto.device.PointDto;
import com.digitaltwin.device.dto.device.PointValueRequest;
import com.digitaltwin.device.dto.device.UpdatePointRequest;
import com.digitaltwin.device.entity.Device;
import com.digitaltwin.device.service.PointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/points")
@RequiredArgsConstructor
@Tag(name = "点位管理", description = "提供点位的增删改查管理接口")
public class PointManagementController {

    private final PointService pointService;

    /**
     * 创建点位
     *
     * @param request 创建点位请求
     * @return 点位信息
     */
    @PostMapping
    public ResponseEntity<ApiResponse> createPoint(@RequestBody CreatePointRequest request) {
        try {
            PointDto pointDto = pointService.createPoint(request);
            return ResponseEntity.ok(ApiResponse.success("Point created successfully", pointDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to create point: " + e.getMessage()));
        }
    }

    /**
     * 根据ID获取点位
     *
     * @param id 点位ID
     * @return 点位信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getPointById(@PathVariable Long id) {
        try {
            PointDto pointDto = pointService.getPointById(id);
            return ResponseEntity.ok(ApiResponse.success("Point retrieved successfully", pointDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve point: " + e.getMessage()));
        }
    }

    /**
     * 根据标识获取点位
     *
     * @param identity 点位标识
     * @return 点位信息
     */
    @GetMapping("/identity/{identity}")
    public ResponseEntity<ApiResponse> getPointByIdentity(@PathVariable String identity) {
        try {
            PointDto pointDto = pointService.getPointByIdentity(identity);
            return ResponseEntity.ok(ApiResponse.success("Point retrieved successfully", pointDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve point: " + e.getMessage()));
        }
    }

    /**
     * 获取所有点位
     *
     * @return 点位列表
     * @deprecated 推荐使用分页接口 /points?page=0&size=10 以提高性能
     */
    @GetMapping
    @Deprecated
    public ResponseEntity<ApiResponse> getAllPoints() {
        try {
            List<PointDto> points = pointService.getAllPoints();
            return ResponseEntity.ok(ApiResponse.success("Points retrieved successfully", points));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve points: " + e.getMessage()));
        }
    }

    /**
     * 分页获取点位列表
     *
     * @param page     页码（从0开始，默认为0）
     * @param size     每页大小（默认为10）
     * @param identity 点位标识（可选，用于模糊匹配）
     * @return 分页的点位列表
     */
    @GetMapping(params = {"page", "size"})
    public ResponseEntity<ApiResponse> getPointsWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String identity,
            @RequestParam(required = false) Boolean published) {
        try {
            // 根据参数组合调用不同的服务方法
            Page<PointDto> points;
            if (identity != null && !identity.isEmpty()) {
                points = pointService.getPointsWithPagination(page, size, identity, published);
            } else if (published != null) {
                // 如果只提供了published参数
                points = pointService.getPointsWithPagination(page, size, null, published);
            } else {
                // 默认查询全部
                points = pointService.getPointsWithPagination(page, size);
            }
            return ResponseEntity.ok(ApiResponse.success("Points retrieved successfully", points));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve points: " + e.getMessage()));
        }
    }

    /**
     * 获取点位总数
     *
     * @return 点位总数
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse> getPointCount() {
        try {
            long count = pointService.getPointCount();
            return ResponseEntity.ok(ApiResponse.success("Point count retrieved successfully", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve point count: " + e.getMessage()));
        }
    }

    /**
     * 更新点位
     *
     * @param id      点位ID
     * @param request 更新点位请求
     * @return 点位信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updatePoint(@PathVariable Long id, @RequestBody UpdatePointRequest request) {
        try {
            PointDto pointDto = pointService.updatePoint(id, request);
            return ResponseEntity.ok(ApiResponse.success("Point updated successfully", pointDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update point: " + e.getMessage()));
        }
    }

    /**
     * 批量删除点位
     *
     * @param ids 点位ID列表
     * @return 操作结果
     */
    @DeleteMapping("/{ids}")
    public ResponseEntity<ApiResponse> deletePoints(@PathVariable List<Long> ids) {
        try {
            pointService.deletePoints(ids);
            return ResponseEntity.ok(ApiResponse.success("Points deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete points: " + e.getMessage()));
        }
    }

    /**
     * 批量删除点位
     *
     * @param ids 点位ID列表
     * @return 操作结果
     */
    @Operation(summary = "批量删除点位", description = "根据点位ID列表批量删除点位信息")
    @PostMapping("/batch-delete")
    public ResponseEntity<ApiResponse> batchDeletePoints(@Valid @RequestBody List<Long> ids) {
        try {
            pointService.deletePoints(ids);
            return ResponseEntity.ok(ApiResponse.success("Points deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete points: " + e.getMessage()));
        }
    }

    /**
     * 设置告警
     *
     * @param request 告警设置请求
     * @return 操作结果
     */
    @PostMapping("/alarm")
    public ResponseEntity<ApiResponse> setAlarm(@RequestBody AlarmSettingRequest request) {
        try {
            pointService.setAlarm(request);
            return ResponseEntity.ok(ApiResponse.success("Alarm setting updated successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update alarm setting: " + e.getMessage()));
        }
    }

    /**
     * 根据点位ID查询其所在分组的所有点位信息
     *
     * @param pointId 点位ID
     * @return 同一分组内的所有点位列表
     */
    @GetMapping("/{pointId}/group-points")
    public ResponseEntity<ApiResponse> getPointsInSameGroup(@PathVariable Long pointId) {
        try {
            List<PointDto> points = pointService.getPointsInSameGroup(pointId);
            return ResponseEntity.ok(ApiResponse.success("Points in same group retrieved successfully", points));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve points in same group: " + e.getMessage()));
        }
    }

    /**
     * 统计每个设备内的点位数量
     *
     * @param published 是否发布（可选参数，不传则统计所有点位）
     * @return 设备点位统计列表
     */
    @GetMapping("/count-by-device")
    public ResponseEntity<ApiResponse> getPointCountByDevice(@RequestParam(required = false) Boolean published) {
        try {
            List<DevicePointCountDto> countList;
            if (published == null) {
                // 未提供published参数，统计所有点位
                countList = pointService.getPointCountByDevice();
            } else {
                // 提供了published参数，按发布状态筛选统计
                countList = pointService.getPointCountByDevice(published);
            }
            return ResponseEntity.ok(ApiResponse.success("Point count by device retrieved successfully", countList));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve point count by device: " + e.getMessage()));
        }
    }

    /**
     * 根据点位ID设置点位值
     *
     * @param pointId 点位ID
     * @param request 点位值请求
     * @return 操作结果
     */
    @PostMapping("/{pointId}/value")
    public ResponseEntity<ApiResponse> setPointValue(@PathVariable Long pointId, @RequestBody PointValueRequest request) {
        try {
            pointService.setPointValue(pointId, request);
            return ResponseEntity.ok(ApiResponse.success("Point value set successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to set point value: " + e.getMessage()));
        }
    }

    /**
     * 批量更新点位的发布状态
     *
     * @param request 批量更新请求
     * @return 更新结果
     */
    @PutMapping("/batch-status")
    public ResponseEntity<ApiResponse> batchUpdatePointsStatus(@RequestBody BatchUpdatePointsStatusRequest request) {
        try {
            List<PointDto> updatedPoints = pointService.updatePointsPublishedStatus(request.getPointIds(), request.getPublished());
            return ResponseEntity.ok(ApiResponse.success("Points published status updated successfully", updatedPoints));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update points published status: " + e.getMessage()));
        }
    }

    /**
     * 根据标识和设备ID查询点位，并以设备为分组返回
     *
     * @param identity 点位标识（可选，模糊匹配）
     * @param deviceId 设备ID（可选，精确匹配）
     * @return 按设备分组的点位列表
     */
    @GetMapping("/grouped-by-device")
    @Operation(summary = "搜索点位并按所属设备分组返回", description = "支持identity模糊匹配和deviceId精确匹配")
    public ResponseEntity<ApiResponse> getPointsGroupedByDevice(
            @RequestParam(required = false) String identity,
            @RequestParam(required = false) Long deviceId) {
        try {
            Map<Device, List<PointDto>> pointsByDevice = pointService.searchPointsGroupedByDevice(identity, deviceId);
            
            // 转换为更友好的返回格式，使用List<Map<String, Object>>结构
            List<Map<String, Object>> result = new ArrayList<>();
            for (Map.Entry<Device, List<PointDto>> entry : pointsByDevice.entrySet()) {
                Map<String, Object> devicePointsInfo = new HashMap<>();
                Device device = entry.getKey();
                
                // 设备信息
                Map<String, Object> deviceInfo = new HashMap<>();
                deviceInfo.put("id", device.getId());
                deviceInfo.put("name", device.getName());
                deviceInfo.put("description", device.getDescription());
                
                devicePointsInfo.put("device", deviceInfo);
                devicePointsInfo.put("points", entry.getValue());
                
                result.add(devicePointsInfo);
            }
            
            return ResponseEntity.ok(ApiResponse.success("Points grouped by device retrieved successfully", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve points grouped by device: " + e.getMessage()));
        }
    }

}