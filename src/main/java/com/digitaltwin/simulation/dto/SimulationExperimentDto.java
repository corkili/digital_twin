package com.digitaltwin.simulation.dto;

import com.digitaltwin.simulation.entity.SimulationExperiment;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor  
@AllArgsConstructor
public class SimulationExperimentDto {
    private Long id;
    private String name;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    
    /**
     * 从实体类转换为DTO
     * @param experiment 实体类
     * @return DTO
     */
    public static SimulationExperimentDto fromEntity(SimulationExperiment experiment) {
        if (experiment == null) {
            return null;
        }
        
        SimulationExperimentDto dto = new SimulationExperimentDto();
        dto.setId(experiment.getId());
        dto.setName(experiment.getName());
        dto.setDescription(experiment.getDescription());
        dto.setStatus(experiment.getStatus());
        dto.setCreatedAt(experiment.getCreatedAt());
        
        return dto;
    }
}