package com.digitaltwin.simulation.dto;

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
public class ExperimentDescriptionDto {
    
    private String name;         // 试验名称
    private String description;  // 试验描述
}