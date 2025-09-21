package com.digitaltwin.simulation.dto;

import com.digitaltwin.simulation.entity.EmergencyProcedure;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonRawValue;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "应急流程信息")
public class EmergencyProcedureDto {

    @Schema(description = "应急流程ID")
    private Long id;

    @Schema(description = "应急流程标题", example = "突发火灾处置")
    private String title;

    @Schema(description = "应急流程描述", example = "火灾应急处理的总体描述")
    private String description;

    @Schema(description = "应急处理步骤，JSON格式数组")
    @JsonRawValue
    private String steps;

    @Schema(description = "状态", example = "ACTIVE")
    private String status;

    public static EmergencyProcedureDto fromEntity(EmergencyProcedure entity) {
        if (entity == null) {
            return null;
        }
        return new EmergencyProcedureDto(
            entity.getId(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getSteps(),
            entity.getStatus()
        );
    }
}