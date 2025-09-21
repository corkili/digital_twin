package com.digitaltwin.websocket.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 测试阶段响应模型
 * 用于WebSocket推送测试阶段信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestPhaseResponse {

    @JsonProperty("phases")
    private List<TestPhaseItem> phases;     // 所有测试阶段列表

    @JsonProperty("currentPhase")
    private String currentPhase;            // 当前激活的测试阶段

    @JsonProperty("timestamp")
    private Long timestamp;                 // 推送时间戳
}