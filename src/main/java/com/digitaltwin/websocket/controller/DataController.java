package com.digitaltwin.websocket.controller;

import com.digitaltwin.device.dto.device.PointDto;
import com.digitaltwin.device.service.PointService;
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

    private final PointService pointService;
    private final TDengineService tdengineService;

    /**
     * 获取历史数据
     * 
     * @param pointIdentity 点位标识
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 历史数据响应
     */
    @GetMapping("/history")
    public WebSocketResponse<HistoryDataResponse> getHistoryData(
            @RequestParam String pointIdentity,
            @RequestParam Long startTime,
            @RequestParam Long endTime) {
        try {
            // 先根据pointIdentity查询PointDto
            PointDto pointDto = pointService.getPointByIdentity(pointIdentity);
            
            // 调用TDengineService查询历史数据（不依赖deviceName）
            List<Map<String, Object>> rawData = tdengineService.querySensorDataByTimeRangeAndPointKey(
                    startTime, endTime, pointIdentity);
            
            // 转换为HistoryDataResponse.DataPoint格式
            List<HistoryDataResponse.DataPoint> historyData = rawData.stream()
                    .map(data -> {
                        java.sql.Timestamp ts = (java.sql.Timestamp) data.get("ts");
                        String value = (String) data.get("point_value");
                        return new HistoryDataResponse.DataPoint(ts.getTime(), value);
                    })
                    .collect(Collectors.toList());
            
            HistoryDataResponse response = new HistoryDataResponse(pointDto, historyData);
            return WebSocketResponse.success(response);
        } catch (Exception e) {
            log.error("查询历史数据失败: {}", e.getMessage(), e);
            return WebSocketResponse.error("查询历史数据失败: " + e.getMessage());
        }
    }
}