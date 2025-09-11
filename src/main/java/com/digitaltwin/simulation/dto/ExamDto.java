package com.digitaltwin.simulation.dto;

import com.digitaltwin.simulation.entity.ExamRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评定考核 DTO")
public class ExamDto {
    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "用户ID", example = "123")
    private Long userId;

    @Schema(description = "用户姓名（联查获取）", example = "张三")
    private String userName;

    @Schema(description = "模式", example = "自动", allowableValues = {"手动", "自动"})
    private String mode;

    @Schema(description = "试验名称", example = "平板材料进行热考核试验")
    private String experimentName;

    @Schema(description = "试验时间", example = "2025-04-15T12:12:12")
    private LocalDateTime experimentTime;

    @Schema(description = "成绩", example = "85")
    private Integer score;

    @Schema(description = "创建时间", example = "2025-04-15T12:12:12")
    private LocalDateTime createdAt;

    public static ExamDto fromEntity(ExamRecord e) {
        if (e == null) return null;
        ExamDto dto = new ExamDto();
        dto.setId(e.getId());
        dto.setUserId(e.getUserId());
        // userName 需要在Service层通过联查设置
        dto.setMode(e.getMode());
        dto.setExperimentName(e.getExperimentName());
        dto.setExperimentTime(e.getExperimentTime());
        dto.setScore(e.getScore());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }
    
    public static ExamDto fromEntityWithUserName(ExamRecord e, String userName) {
        ExamDto dto = fromEntity(e);
        if (dto != null) {
            dto.setUserName(userName);
        }
        return dto;
    }
}

