package com.digitaltwin.websocket.model;

import com.digitaltwin.device.dto.device.PointDto;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 历史数据响应DTO
 */
@Data
@NoArgsConstructor
public class HistoryDataResponse {
    /**
     * 点位信息
     */
    private PointDto pointInfo;
    
    /**
     * 历史数据列表
     * 每个元素包含时间戳和点位数据值
     */
    private List<DataPoint> historyData;
    
    @Data
    @NoArgsConstructor
    public static class DataPoint {
        /**
         * 毫秒级时间戳
         */
        private Long timestamp;
        
        /**
         * 点位数据值
         */
        private String value;
        
        public DataPoint(Long timestamp, String value) {
            this.timestamp = timestamp;
            this.value = value;
        }
    }
    
    public HistoryDataResponse(PointDto pointInfo, List<DataPoint> historyData) {
        this.pointInfo = pointInfo;
        this.historyData = historyData;
    }
}