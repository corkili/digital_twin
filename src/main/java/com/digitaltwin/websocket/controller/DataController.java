package com.digitaltwin.websocket.controller;

import com.digitaltwin.device.dto.device.PointDto;
import com.digitaltwin.device.entity.Device;
import com.digitaltwin.device.repository.DeviceRepository;
import com.digitaltwin.device.repository.PointRepository;
import com.digitaltwin.websocket.model.HistoryDataResponse;
import com.digitaltwin.websocket.model.WebSocketResponse;
import com.digitaltwin.websocket.service.TDengineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 历史数据控制器
 * 提供REST API接口处理历史数据查询
 */
@Slf4j
@RestController
@RequestMapping("/data")
@RequiredArgsConstructor
public class DataController {

    private final PointRepository pointRepository;
    private final DeviceRepository deviceRepository;
    private final TDengineService tdengineService;

    /**
     * 根据点位identity获取特定点位在一段时间内的数据
     *
     * @param startTime 起始时间，秒级时间戳
     * @param endTime 结束时间，秒级时间戳
     * @param pointIdentity 点位标识
     * @param deviceId 设备id
     * @return 历史数据响应
     */
    @GetMapping("/history")
    public WebSocketResponse<HistoryDataResponse> getHistoryData(
            @RequestParam long startTime,
            @RequestParam long endTime,
            @RequestParam String pointIdentity,
            @RequestParam Long deviceId) {
        try {
            log.info("获取历史数据请求: startTime={}, endTime={}, pointIdentity={}, deviceId={}", 
                    startTime, endTime, pointIdentity, deviceId);

            // 1. 通过pointIdentity和deviceId查询到Point信息
            PointDto pointDto = pointRepository.findByIdentityAndDeviceId(pointIdentity, deviceId)
                    .map(PointDto::new)
                    .orElseThrow(() -> new RuntimeException("未找到点位信息: pointIdentity=" + pointIdentity + ", deviceId=" + deviceId));

            // 2. 通过deviceId查询到deviceName
            String deviceName = deviceRepository.findById(deviceId)
                    .map(Device::getName)
                    .orElseThrow(() -> new RuntimeException("未找到设备信息: deviceId=" + deviceId));

            // 3. 通过TDengineService查询数据
            List<Map<String, Object>> rawData = tdengineService.querySensorDataByTimeRangeAndPointKeyAndDeviceName(
                    startTime * 1000, endTime * 1000, pointIdentity, deviceName);

            // 4. 封装历史数据
            List<HistoryDataResponse.DataPoint> historyData = rawData.stream()
                    .map(row -> {
                        java.sql.Timestamp ts = (java.sql.Timestamp) row.get("ts");
                        String value = (String) row.get("point_value");
                        return new HistoryDataResponse.DataPoint(ts.getTime(), value);
                    })
                    .collect(Collectors.toList());

            // 5. 封装response返回
            HistoryDataResponse response = new HistoryDataResponse(pointDto, historyData);
            return WebSocketResponse.success(response);

        } catch (Exception e) {
            log.error("获取历史数据失败: {}", e.getMessage(), e);
            return WebSocketResponse.error("获取历史数据失败: " + e.getMessage());
        }
    }
}