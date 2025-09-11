package com.digitaltwin.trial.dto;

import lombok.Data;

@Data
public class TrailCountResponse {
    private Long totalCount;

    public TrailCountResponse() {}

    public TrailCountResponse(Long totalCount) {
        this.totalCount = totalCount;
    }
}