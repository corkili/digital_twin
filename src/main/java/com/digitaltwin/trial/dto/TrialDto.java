package com.digitaltwin.trial.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TrialDto {
    private Long id;
    private String name;
    private String runNo;
    private String mode;
    private Long startTimestamp;
    private Long endTimestamp;
    private String startTime;
    private String endTime;

    // Constructors
    public TrialDto() {}

    public TrialDto(Long id, String name, String runNo, String mode, Long startTimestamp, Long endTimestamp) {
        this.id = id;
        this.name = name;
        this.runNo = runNo;
        this.mode = mode;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        
        // 格式化时间
        if (startTimestamp != null) {
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTimestamp), ZoneId.systemDefault());
            this.startTime = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        if (endTimestamp != null) {
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(endTimestamp), ZoneId.systemDefault());
            this.endTime = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRunNo() {
        return runNo;
    }

    public void setRunNo(String runNo) {
        this.runNo = runNo;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public Long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(Long startTimestamp) {
        this.startTimestamp = startTimestamp;
        // 更新格式化时间
        if (startTimestamp != null) {
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startTimestamp), ZoneId.systemDefault());
            this.startTime = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }

    public Long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(Long endTimestamp) {
        this.endTimestamp = endTimestamp;
        // 更新格式化时间
        if (endTimestamp != null) {
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(endTimestamp), ZoneId.systemDefault());
            this.endTime = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "TrialDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", runNo='" + runNo + '\'' +
                ", mode='" + mode + '\'' +
                ", startTimestamp=" + startTimestamp +
                ", endTimestamp=" + endTimestamp +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                '}';
    }
}