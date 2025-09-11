package com.digitaltwin.trial.dto;

import com.digitaltwin.trial.entity.Trial;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
public class TrailListResponse {
    private Long totalCount;
    private List<TrailListItem> trailList;

    public TrailListResponse() {}

    public TrailListResponse(Long totalCount, List<TrailListItem> trailList) {
        this.totalCount = totalCount;
        this.trailList = trailList;
    }

    @Data
    public static class TrailListItem {
        private Long id;
        private String name;
        private String runNo;
        private String mode;
        private Long startTimestamp;
        private Long endTimestamp;
        private String startTime;
        private String endTime;

        public TrailListItem() {}

        public TrailListItem(Trial trial) {
            this.id = trial.getId();
            this.name = trial.getName();
            this.runNo = trial.getRunNo();
            this.mode = trial.getMode();
            this.startTimestamp = trial.getStartTimestamp();
            this.endTimestamp = trial.getEndTimestamp();
            
            // 格式化时间
            if (trial.getStartTimestamp() != null) {
                LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(trial.getStartTimestamp()), ZoneId.systemDefault());
                this.startTime = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
            if (trial.getEndTimestamp() != null) {
                LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(trial.getEndTimestamp()), ZoneId.systemDefault());
                this.endTime = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
        }
    }
}