package com.digitaltwin.trial.dto;

import java.util.Map;

import lombok.Data;

@Data
public class TrailHistoryData {
    private Long timestamp;
    private Map<String, String> pointsData;

    public TrailHistoryData() {
    }

    public TrailHistoryData(Long timestamp, Map<String, String> pointsData) {
        this.timestamp = timestamp;
        this.pointsData = pointsData;
    }

}