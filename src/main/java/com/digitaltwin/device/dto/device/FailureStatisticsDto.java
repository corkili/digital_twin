package com.digitaltwin.device.dto.device;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class FailureStatisticsDto {
    private LocalDate date;
    private long count;

    public FailureStatisticsDto(LocalDate date, long count) {
        this.date = date;
        this.count = count;
    }
}