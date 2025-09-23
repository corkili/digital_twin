package com.digitaltwin.device.dto.device;

import com.digitaltwin.device.entity.FaultCategory;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FaultCategoryDto {
    private Long id;
    private String faultType;
    private String description;
    private Integer trainingDifficulty;

    public FaultCategoryDto(FaultCategory faultCategory) {
        this.id = faultCategory.getId();
        this.faultType = faultCategory.getFaultType();
        this.description = faultCategory.getDescription();
        this.trainingDifficulty = faultCategory.getTrainingDifficulty();
    }
}