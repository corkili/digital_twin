package com.digitaltwin.simulation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

/**
 * 提交试验步骤请求DTO
 */
@Data
@Schema(description = "提交试验步骤请求")
public class SubmitExperimentStepRequest {
    
    @Schema(description = "用户ID", required = true, example = "123")
    private Long userId;
    
    @Schema(description = "用户AUAP ID", required = true, example = "auap_user_001")
    private String auapUserId;
    
    @Schema(description = "基于的试验模板ID", required = true, example = "456")
    private Long targetExperimentId;
    
    @Schema(description = "用户操作的试验步骤数据（步骤数组）", required = true)
    private List<ExperimentStepDto> experimentSteps;
}