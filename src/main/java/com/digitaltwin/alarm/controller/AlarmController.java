package com.digitaltwin.alarm.controller;

import com.digitaltwin.alarm.dto.AlarmCountResponse;
import com.digitaltwin.alarm.dto.AlarmDetailResponse;
import com.digitaltwin.alarm.dto.AlarmListResponse;
import com.digitaltwin.alarm.entity.Alarm;
import com.digitaltwin.alarm.entity.AlarmState;
import com.digitaltwin.alarm.service.AlarmDetailService;
import com.digitaltwin.alarm.service.AlarmQueryService;
import com.digitaltwin.device.entity.Device;
import com.digitaltwin.device.entity.Point;
import com.digitaltwin.device.repository.DeviceRepository;
import com.digitaltwin.device.repository.PointRepository;
import com.digitaltwin.websocket.model.WebSocketResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import java.util.Collections;

@Slf4j
@RestController
@RequestMapping("/alarms")
@RequiredArgsConstructor
@Tag(name = "告警管理", description = "提供告警信息的查询和统计接口，支持按设备、点位、传感器等维度查询告警数据")
public class AlarmController {

    private final AlarmQueryService alarmQueryService;
    private final AlarmDetailService alarmDetailService;
    private final DeviceRepository deviceRepository;
    private final PointRepository pointRepository;

    /**
     * 根据设备ID获取告警
     *
     * @param deviceId 设备ID
     * @return 告警列表
     */
    @Operation(summary = "根据设备ID获取告警", description = "查询指定设备的所有告警信息")
    @GetMapping("/device/{deviceId}")
    public WebSocketResponse<List<Alarm>> getAlarmsByDeviceId(
            @Parameter(description = "设备ID", required = true) @PathVariable Long deviceId) {
        try {
            List<Alarm> alarms = alarmQueryService.getAlarmsByDeviceId(deviceId);
            return WebSocketResponse.success(alarms);
        } catch (Exception e) {
            log.error("获取设备告警失败: {}", e.getMessage(), e);
            return WebSocketResponse.error("获取设备告警失败: " + e.getMessage());
        }
    }

    /**
     * 根据点位标识获取告警
     *
     * @param pointId 点位标识
     * @return 告警列表
     */
    @Operation(summary = "根据点位ID获取告警", description = "查询指定点位的所有告警信息")
    @GetMapping("/point/{pointId}")
    public WebSocketResponse<List<Alarm>> getAlarmsByPointId(
            @Parameter(description = "点位标识", required = true) @PathVariable String pointId) {
        try {
            List<Alarm> alarms = alarmQueryService.getAlarmsByPointId(pointId);
            return WebSocketResponse.success(alarms);
        } catch (Exception e) {
            log.error("获取点位告警失败: {}", e.getMessage(), e);
            return WebSocketResponse.error("获取点位告警失败: " + e.getMessage());
        }
    }

    /**
     * 根据传感器ID获取告警
     *
     * @param sensorId 传感器ID
     * @return 告警列表
     */
    @Operation(summary = "根据传感器ID获取告警", description = "查询指定传感器的所有告警信息")
    @GetMapping("/sensor/{sensorId}")
    public WebSocketResponse<List<Alarm>> getAlarmsBySensorId(
            @Parameter(description = "传感器ID", required = true) @PathVariable String sensorId) {
        try {
            List<Alarm> alarms = alarmQueryService.getAlarmsBySensorId(sensorId);
            return WebSocketResponse.success(alarms);
        } catch (Exception e) {
            log.error("获取传感器告警失败: {}", e.getMessage(), e);
            return WebSocketResponse.error("获取传感器告警失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有告警
     *
     * @return 告警列表
     */
    @Operation(summary = "获取所有告警", description = "查询系统中的所有告警信息")
    @GetMapping("/all")
    public WebSocketResponse<List<Alarm>> getAllAlarms() {
        try {
            List<Alarm> alarms = alarmQueryService.getAllAlarms();
            return WebSocketResponse.success(alarms);
        } catch (Exception e) {
            log.error("获取所有告警失败: {}", e.getMessage(), e);
            return WebSocketResponse.error("获取所有告警失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据告警状态获取告警
     *
     * @param state 告警状态
     * @return 告警列表
     */
    @Operation(summary = "根据告警状态获取告警", description = "按照告警状态筛选查询告警信息")
    @GetMapping("/state/{state}")
    public WebSocketResponse<List<Alarm>> getAlarmsByState(
            @Parameter(description = "告警状态", required = true) @PathVariable AlarmState state) {
        try {
            List<Alarm> alarms = alarmQueryService.getAlarmsByState(state);
            return WebSocketResponse.success(alarms);
        } catch (Exception e) {
            log.error("根据告警状态获取告警失败: {}", e.getMessage(), e);
            return WebSocketResponse.error("获取告警失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据告警状态和设备ID获取告警
     *
     * @param state 告警状态
     * @param deviceId 设备ID
     * @return 告警列表
     */
    @Operation(summary = "根据状态和设备ID获取告警", description = "按照告警状态和设备ID组合条件查询告警信息")
    @GetMapping("/state/{state}/device/{deviceId}")
    public WebSocketResponse<List<Alarm>> getAlarmsByStateAndDeviceId(
            @Parameter(description = "告警状态", required = true) @PathVariable AlarmState state,
            @Parameter(description = "设备ID", required = true) @PathVariable Long deviceId) {
        try {
            List<Alarm> alarms = alarmQueryService.getAlarmsByStateAndDeviceId(state, deviceId);
            return WebSocketResponse.success(alarms);
        } catch (Exception e) {
            log.error("根据告警状态和设备ID获取告警失败: {}", e.getMessage(), e);
            return WebSocketResponse.error("获取告警失败: " + e.getMessage());
        }
    }

    /**
     * 根据时间范围获取告警总次数
     *
     * @param timeRange 时间范围（今日、本周、本月、全年），若未传该参数或该参数的值为空字符串，则查询所有的告警总数
     * @return 告警总次数
     */
    @Operation(summary = "获取告警统计数量", description = "按时间范围统计告警总次数，支持今日、本周、本月、全年等时间范围")
    @GetMapping("/count")
    public WebSocketResponse<AlarmCountResponse> getAlarmCountByTimeRange(
            @Parameter(description = "时间范围（今日、本周、本月、全年），为空则查询所有告警", required = false) @RequestParam(required = false) String timeRange) {
        try {
            Long count;
            if (timeRange == null || timeRange.isEmpty()) {
                count = alarmQueryService.getAllAlarmCount();
            } else {
                count = alarmQueryService.getAlarmCountByTimeRange(timeRange);
            }
            return WebSocketResponse.success(new AlarmCountResponse(count));
        } catch (Exception e) {
            log.error("根据时间范围获取告警总次数失败: {}", e.getMessage(), e);
            return WebSocketResponse.error("获取告警总次数失败: " + e.getMessage());
        }
    }

    /**
     * 根据时间范围获取告警列表
     *
     * @param timeRange 时间范围（今日、本周、本月、全年）
     * @param page 页码（从0开始）
     * @param size 每页数量
     * @param deviceId 设备ID（可选）
     * @return 告警列表
     */
    @Operation(summary = "分页查询告警列表", description = "按时间范围和设备ID分页查询告警列表，支持多种查询条件组合")
    @GetMapping("/list")
    public WebSocketResponse<AlarmListResponse> getAlarmsByTimeRange(
            @Parameter(description = "时间范围（今日、本周、本月、全年）", required = false) @RequestParam(required = false) String timeRange,
            @Parameter(description = "页码（从0开始）", required = true) @RequestParam int page,
            @Parameter(description = "每页数量", required = true) @RequestParam int size,
            @Parameter(description = "设备ID（可选）", required = false) @RequestParam(required = false) Long deviceId) {
        try {
            // 获取总次数和告警列表
            Long totalCount;
            List<Alarm> alarms;
            
            if (deviceId != null) {
                // 根据设备ID查询
                alarms = alarmQueryService.getAlarmsByDeviceIdWithPagination(deviceId, page + 1, size);
                totalCount = alarmQueryService.getAlarmCountByDeviceId(deviceId);
            } else if (timeRange != null && !timeRange.isEmpty()) {
                // 根据时间范围查询
                totalCount = alarmQueryService.getAlarmCountByTimeRange(timeRange);
                alarms = alarmQueryService.getAlarmsByTimeRange(timeRange, page + 1, size);
            } else {
                // 查询所有告警
                totalCount = alarmQueryService.getAllAlarmCount();
                alarms = alarmQueryService.getAllAlarmsWithPagination(page + 1, size);
            }
            
            // 收集所有设备ID
            List<Long> deviceIds = alarms.stream()
                    .map(Alarm::getDeviceId)
                    .distinct()
                    .collect(Collectors.toList());
            
            // 批量查询设备
            List<Device> devices = deviceRepository.findByIds(deviceIds);
            
            // 创建设备ID到设备名称的映射
            java.util.Map<Long, String> deviceIdToNameMap = devices.stream()
                    .collect(Collectors.toMap(Device::getId, Device::getName));
            
            // 收集所有点位ID
            List<String> pointIds = alarms.stream()
                    .map(Alarm::getPointId)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            
            // 批量查询点位
            List<Point> points = pointIds.isEmpty() ? Collections.emptyList() : pointRepository.findByIdentityIn(pointIds);
            
            // 创建点位ID到点位的映射，处理重复键的情况 TODO: AI修改，不置可否
            java.util.Map<String, Point> pointIdToPointMap = points.stream()
                    .collect(Collectors.toMap(Point::getIdentity, point -> point, (existing, replacement) -> existing));
            
            // 转换为AlarmListResponse.AlarmListItem列表
            List<AlarmListResponse.AlarmListItem> alarmListItems = alarms.stream()
                    .map(alarm -> {
                        AlarmListResponse.AlarmListItem item = new AlarmListResponse.AlarmListItem(alarm);
                        // 设置设备名称
                        String deviceName = deviceIdToNameMap.get(alarm.getDeviceId());
                        if (deviceName != null) {
                            item.setDeviceName(deviceName);
                        } else {
                            item.setDeviceName("未知设备-" + alarm.getDeviceId());
                        }
                        
                        // 设置通道名称和点位名称
                        Point point = pointIdToPointMap.get(alarm.getPointId());
                        if (point != null) {
                            item.setPointName(point.getIdentity());
                            Device device = point.getDevice();
                            if (device != null && device.getChannel() != null) {
                                item.setChannelName(device.getChannel().getName());
                            }
                        }
                        
                        return item;
                    })
                    .collect(Collectors.toList());
            
            // 创建AlarmListResponse对象
            AlarmListResponse response = new AlarmListResponse(totalCount, alarmListItems);
            
            return WebSocketResponse.success(response);
        } catch (Exception e) {
            log.error("根据时间范围获取告警列表失败: {}", e.getMessage(), e);
            return WebSocketResponse.error("获取告警列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据告警ID获取告警详情
     *
     * @param alarmId 告警ID
     * @param needOperateLog 是否需要操作日志
     * @param operateLogLimit 操作日志数量限制
     * @return 告警详情
     */
    @Operation(summary = "获取告警详情", description = "根据告警ID查询告警的详细信息，可选择是否包含操作日志")
    @GetMapping("/detail/{alarmId}")
    public WebSocketResponse<AlarmDetailResponse> getAlarmDetail(
            @Parameter(description = "告警ID", required = true) @PathVariable Long alarmId,
            @Parameter(description = "是否需要操作日志", required = false) @RequestParam(defaultValue = "false") boolean needOperateLog,
            @Parameter(description = "操作日志数量限制", required = false) @RequestParam(required = false) Integer operateLogLimit) {
        try {
            AlarmDetailResponse response = alarmDetailService.getAlarmDetail(alarmId, needOperateLog, operateLogLimit);
            return WebSocketResponse.success(response);
        } catch (Exception e) {
            log.error("获取告警详情失败: {}", e.getMessage(), e);
            return WebSocketResponse.error("获取告警详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据设备ID获取最新告警详情
     *
     * @param deviceId 设备ID
     * @return 告警详情
     */
    @Operation(summary = "获取设备最新告警详情", description = "查询指定设备的最新告警详细信息")
    @GetMapping("/latest/device/{deviceId}")
    public WebSocketResponse<AlarmDetailResponse> getLatestAlarmDetailByDeviceId(
            @Parameter(description = "设备ID", required = true) @PathVariable Long deviceId) {
        try {
            AlarmDetailResponse response = alarmDetailService.getLatestAlarmDetailByDeviceId(deviceId);
            return WebSocketResponse.success(response);
        } catch (Exception e) {
            log.error("获取设备最新告警详情失败: {}", e.getMessage(), e);
            return WebSocketResponse.error("获取设备最新告警详情失败: " + e.getMessage());
        }
    }
}