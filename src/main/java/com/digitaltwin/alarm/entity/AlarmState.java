package com.digitaltwin.alarm.entity;

public enum AlarmState {
    UNCONFIRMED("未确认"),
    CONFIRMED("已确认"),
    IGNORED("已忽略");
    
    private final String description;
    
    AlarmState(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}