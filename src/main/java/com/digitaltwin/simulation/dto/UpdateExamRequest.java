package com.digitaltwin.simulation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "更新评定考核请求")
public class UpdateExamRequest {
    @Schema(description = "用户AUAP ID")
    private String auapUserId;

    @Schema(description = "模式(手动/自动)")
    private String mode;

    @Schema(description = "试验名称")
    private String experimentName;

    @Schema(description = "试验时间")
    private LocalDateTime experimentTime;

    @Schema(description = "成绩")
    private Integer score;
}

