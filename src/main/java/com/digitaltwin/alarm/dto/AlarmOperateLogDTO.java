package com.digitaltwin.alarm.dto;

import lombok.Data;

@Data
public class AlarmOperateLogDTO {
    private Long operateTimestamp; // 毫秒级时间戳
    private String operateAction;
}