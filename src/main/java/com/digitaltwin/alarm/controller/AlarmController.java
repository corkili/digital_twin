package com.digitaltwin.alarm.controller;

import com.digitaltwin.alarm.dto.AlarmCountResponse;
import com.digitaltwin.alarm.dto.AlarmListResponse;
import com.digitaltwin.alarm.entity.Alarm;
import com.digitaltwin.alarm.entity.AlarmState;
import com.digitaltwin.alarm.service.AlarmQueryService;
import com.digitaltwin.device.entity.Device;
import com.digitaltwin.device.repository.DeviceRepository;
import com.digitaltwin.websocket.model.WebSocketResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/alarms")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmQueryService alarmQueryService;
    private final DeviceRepository deviceRepository;

    /**
     * 根据设备ID获取告警
     * 
     * @param deviceId 设备ID
     * @return 告警列表
     */
    @GetMapping("/device/{deviceId}")
    public WebSocketResponse<List<Alarm>> getAlarmsByDeviceId(@PathVariable Long deviceId) {
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
    @GetMapping("/point/{pointId}")
    public WebSocketResponse<List<Alarm>> getAlarmsByPointId(@PathVariable String pointId) {
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
    @GetMapping("/sensor/{sensorId}")
    public WebSocketResponse<List<Alarm>> getAlarmsBySensorId(@PathVariable String sensorId) {
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
    @GetMapping("/state/{state}")
    public WebSocketResponse<List<Alarm>> getAlarmsByState(@PathVariable AlarmState state) {
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
    @GetMapping("/state/{state}/device/{deviceId}")
    public WebSocketResponse<List<Alarm>> getAlarmsByStateAndDeviceId(
            @PathVariable AlarmState state, 
            @PathVariable Long deviceId) {
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
     * @param timeRange 时间范围（今日、本周、本月、全年）
     * @return 告警总次数
     */
    @GetMapping("/count")
    public WebSocketResponse<AlarmCountResponse> getAlarmCountByTimeRange(
            @RequestParam String timeRange) {
        try {
            Long count = alarmQueryService.getAlarmCountByTimeRange(timeRange);
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
     * @param pageNum 页码（从1开始）
     * @param pageCount 每页数量
     * @return 告警列表
     */
    @GetMapping("/list")
    public WebSocketResponse<AlarmListResponse> getAlarmsByTimeRange(
            @RequestParam String timeRange,
            @RequestParam int pageNum,
            @RequestParam int pageCount) {
        try {
            // 获取总次数
            Long totalCount = alarmQueryService.getAlarmCountByTimeRange(timeRange);
            
            // 获取告警列表
            List<Alarm> alarms = alarmQueryService.getAlarmsByTimeRange(timeRange, pageNum, pageCount);
            
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
}