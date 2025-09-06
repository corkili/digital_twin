package com.digitaltwin.simulation.dto;

import com.digitaltwin.simulation.entity.SimulationExperiment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "仿真试验列表项")
public class SimulationExperimentListDto {
    @Schema(description = "试验ID", example = "1")
    private Long id;
    
    @Schema(description = "试验名称", example = "传感器校准试验")
    private String name;
    
    @Schema(description = "试验状态", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
    private String status;
    
    @Schema(description = "创建时间", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
    
    /**
     * 从实体类转换为列表DTO
     * @param experiment 实体类
     * @return 列表DTO
     */
    public static SimulationExperimentListDto fromEntity(SimulationExperiment experiment) {
        if (experiment == null) {
            return null;
        }
        
        SimulationExperimentListDto dto = new SimulationExperimentListDto();
        dto.setId(experiment.getId());
        dto.setName(experiment.getName());
        dto.setStatus(experiment.getStatus());
        dto.setCreatedAt(experiment.getCreatedAt());
        
        return dto;
    }
}