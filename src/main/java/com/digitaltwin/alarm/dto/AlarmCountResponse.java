package com.digitaltwin.alarm.dto;

import lombok.Data;

@Data
public class AlarmCountResponse {
    private Long totalCount;

    public AlarmCountResponse() {}

    public AlarmCountResponse(Long totalCount) {
        this.totalCount = totalCount;
    }
}