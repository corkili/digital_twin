package com.digitaltwin.simulation.dto;

import com.digitaltwin.simulation.entity.ExperimentComponent;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonRawValue;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "试验组件信息")
public class ExperimentComponentDto {

    @Schema(description = "试验组件ID")
    private Long id;

    @Schema(description = "组件名称", example = "传感器组件集")
    private String name;

    @Schema(description = "组件描述", example = "包含温度、压力、湿度等传感器组件")
    private String description;

    @Schema(description = "组件列表数据，JSON格式")
    @JsonRawValue
    private String componentList;

    @Schema(description = "状态", example = "ACTIVE")
    private String status;

    public static ExperimentComponentDto fromEntity(ExperimentComponent entity) {
        if (entity == null) {
            return null;
        }
        return new ExperimentComponentDto(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            entity.getComponentList(),
            entity.getStatus()
        );
    }
}