package com.digitaltwin.websocket.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 测试阶段项模型
 * 用于表示单个测试阶段的状态
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestPhaseItem {

    @JsonProperty("key")
    private String key;              // 测试阶段的中文名称

    @JsonProperty("current")
    private boolean current;         // 是否为当前阶段
}