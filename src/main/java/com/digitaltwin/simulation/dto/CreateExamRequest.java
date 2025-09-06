package com.digitaltwin.simulation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "创建评定考核请求")
public class CreateExamRequest {
    @Schema(description = "姓名", required = true)
    private String name;

    @Schema(description = "模式(手动/自动)", example = "自动")
    private String mode;

    @Schema(description = "试验名称")
    private String experimentName;

    @Schema(description = "试验时间")
    private LocalDateTime experimentTime;
}

