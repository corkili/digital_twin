package com.digitaltwin.trial.dto;

import java.util.Map;

import lombok.Data;

@Data
public class TrailHistoryData {
    private Long timestamp;
    private Map<String, String> pointsData;
    private String subscribeId;

    public TrailHistoryData() {
    }

    public TrailHistoryData(Long timestamp, Map<String, String> pointsData) {
        this.timestamp = timestamp;
        this.pointsData = pointsData;
    }

    public TrailHistoryData(Long timestamp, Map<String, String> pointsData, String subscribeId) {
        this.timestamp = timestamp;
        this.pointsData = pointsData;
        this.subscribeId = subscribeId;
    }

}