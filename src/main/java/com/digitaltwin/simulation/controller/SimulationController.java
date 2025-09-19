package com.digitaltwin.simulation.controller;

import com.digitaltwin.simulation.dto.SimulationApiResponse;
import com.digitaltwin.simulation.dto.SimulationExperimentListDto;
import com.digitaltwin.simulation.dto.ExperimentStepsDto;
import com.digitaltwin.simulation.dto.ExperimentStepsResponseDto;
import com.digitaltwin.simulation.dto.ExperimentDescriptionDto;
import com.digitaltwin.simulation.dto.SubmitExperimentStepRequest;
import com.digitaltwin.simulation.service.SimulationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 仿真试验控制器
 * 提供模拟试验相关的REST API接口
 */
@Slf4j
@RestController
@RequestMapping("/simulations")
@RequiredArgsConstructor
@Tag(name = "仿真试验管理", description = "提供仿真试验的查询接口，包括试验列表、介绍和步骤信息")
public class SimulationController {
    
    private final SimulationService simulationService;
    
    /**
     * 获取模拟试验列表
     * 
     * @return 试验列表
     */
    @GetMapping
    public ResponseEntity<SimulationApiResponse<List<SimulationExperimentListDto>>> getAllExperiments() {
        try {
            List<SimulationExperimentListDto> experiments = simulationService.getAllExperiments();
            return ResponseEntity.ok(SimulationApiResponse.success("获取试验列表成功", experiments));
        } catch (Exception e) {
            log.error("获取试验列表失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SimulationApiResponse.error("获取试验列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 根据试验ID获取试验介绍
     * 
     * @param id 试验ID
     * @return 试验名称和介绍
     */
    @GetMapping("/{id}/description")
    public ResponseEntity<SimulationApiResponse<ExperimentDescriptionDto>> getExperimentDescription(
            @Parameter(description = "试验ID") @PathVariable Long id) {
        try {
            Optional<ExperimentDescriptionDto> description = simulationService.getExperimentDescription(id);
            if (description.isPresent()) {
                return ResponseEntity.ok(SimulationApiResponse.success("获取试验介绍成功", description.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(SimulationApiResponse.error("未找到ID为 " + id + " 的试验"));
            }
        } catch (Exception e) {
            log.error("获取试验介绍失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SimulationApiResponse.error("获取试验介绍失败: " + e.getMessage()));
        }
    }

    /**
     * 根据试验ID获取试验步骤（包含手动和自动模式所有数据）
     *
     * @param id 试验ID
     * @param shuffle 是否乱序，可选参数，默认为false
     * @return 试验步骤数据，包含手动步骤、试验流程、应急流程，由前端选择展示
     */
    @Operation(summary = "获取试验步骤", description = "获取试验的所有步骤数据，包含手动模式步骤和自动模式的试验流程、应急流程，前端根据选项决定展示哪个")
    @GetMapping("/{id}/steps")
    public ResponseEntity<SimulationApiResponse<ExperimentStepsResponseDto>> getExperimentSteps(
            @Parameter(description = "试验ID") @PathVariable Long id,
            @Parameter(description = "是否乱序，默认为false") @RequestParam(value = "shuffle", required = false, defaultValue = "false") Boolean shuffle) {
        try {
            Optional<ExperimentStepsResponseDto> steps = simulationService.getExperimentStepsV2(id, shuffle);
            if (steps.isPresent()) {
                return ResponseEntity.ok(SimulationApiResponse.success("获取试验步骤成功", steps.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(SimulationApiResponse.error("未找到ID为 " + id + " 的试验步骤"));
            }
        } catch (Exception e) {
            log.error("获取试验步骤失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SimulationApiResponse.error("获取试验步骤失败: " + e.getMessage()));
        }
    }

    /**
     * 根据试验ID获取试验步骤（原版本，向后兼容）
     *
     * @param id 试验ID
     * @param shuffle 是否乱序，可选参数，默认为false
     * @return 试验步骤数据（仅支持手动模式）
     */
    @Operation(summary = "获取试验步骤（兼容版本）", description = "获取手动模式的试验步骤，保持向后兼容")
    @GetMapping("/{id}/steps/legacy")
    public ResponseEntity<SimulationApiResponse<ExperimentStepsDto>> getExperimentStepsLegacy(
            @Parameter(description = "试验ID") @PathVariable Long id,
            @Parameter(description = "是否乱序，默认为false") @RequestParam(value = "shuffle", required = false, defaultValue = "false") Boolean shuffle) {
        try {
            Optional<ExperimentStepsDto> steps = simulationService.getExperimentSteps(id, shuffle);
            if (steps.isPresent()) {
                return ResponseEntity.ok(SimulationApiResponse.success("获取试验步骤成功", steps.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(SimulationApiResponse.error("未找到ID为 " + id + " 的试验步骤"));
            }
        } catch (Exception e) {
            log.error("获取试验步骤失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SimulationApiResponse.error("获取试验步骤失败: " + e.getMessage()));
        }
    }
    
    /**
     * 提交试验步骤
     * 
     * @param request 提交请求，包含用户ID、试验ID和步骤数据
     * @return 提交结果
     */
    @Operation(summary = "提交试验步骤", description = "用户提交基于某个试验模板的操作步骤数据")
    @PostMapping("/submit")
    public ResponseEntity<SimulationApiResponse<Long>> submitExperimentStep(
            @RequestBody SubmitExperimentStepRequest request) {
        try {
            Long recordId = simulationService.submitExperimentStep(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(SimulationApiResponse.success("提交试验步骤成功", recordId));
        } catch (Exception e) {
            log.error("提交试验步骤失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SimulationApiResponse.error("提交试验步骤失败: " + e.getMessage()));
        }
    }
}