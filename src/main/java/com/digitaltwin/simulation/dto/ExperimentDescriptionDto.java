package com.digitaltwin.simulation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 试验描述DTO
 * 包含试验名称和描述信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "试验描述信息")
public class ExperimentDescriptionDto {
    
    @Schema(description = "试验名称", example = "传感器校准试验")
    private String name;
    
    @Schema(description = "试验详细描述", example = "该试验旨在验证传感器在不同环境下的校准精度和稳定性")
    private String description;
}