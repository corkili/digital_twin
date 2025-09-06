package com.digitaltwin.simulation.dto;

import com.digitaltwin.simulation.entity.SimulationExperiment;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulationExperimentListDto {
    private Long id;
    private String name;
    private String status;
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