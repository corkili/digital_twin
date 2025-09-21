package com.digitaltwin.simulation.controller;

import com.digitaltwin.simulation.dto.SimulationApiResponse;
import com.digitaltwin.simulation.dto.SimulationExperimentListDto;
import com.digitaltwin.simulation.dto.ExperimentStepsDto;
import com.digitaltwin.simulation.dto.ExperimentStepsResponseDto;
import com.digitaltwin.simulation.dto.ExperimentDescriptionDto;
import com.digitaltwin.simulation.dto.SubmitExperimentStepRequest;
import com.digitaltwin.simulation.dto.EmergencyProcedureDto;
import com.digitaltwin.simulation.dto.ExperimentComponentDto;
import com.digitaltwin.simulation.enums.RoleType;
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
     * 根据试验ID获取试验步骤
     *
     * @param id 试验ID
     * @param shuffle 是否乱序，可选参数，默认为false
     * @param role 角色类型过滤，可选参数，支持"data_operator"和"commander"，为空时返回全量数据
     * @return 试验步骤数据，包含手动步骤
     */
    @Operation(summary = "获取试验步骤", description = "获取试验的步骤数据，包含手动模式步骤，支持按角色类型过滤")
    @GetMapping("/{id}/steps")
    public ResponseEntity<SimulationApiResponse<ExperimentStepsResponseDto>> getExperimentSteps(
            @Parameter(description = "试验ID") @PathVariable Long id,
            @Parameter(description = "是否乱序，默认为false") @RequestParam(value = "shuffle", required = false, defaultValue = "false") Boolean shuffle,
            @Parameter(description = "角色类型过滤，支持'data_operator'和'commander'，为空时返回全量数据") @RequestParam(value = "role", required = false) String role) {
        try {
            RoleType roleType = null;
            if (role != null && !role.trim().isEmpty()) {
                roleType = RoleType.fromDisplayName(role.trim());
                if (roleType == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(SimulationApiResponse.error("不支持的角色类型: " + role + "，支持的角色类型: data_operator, commander"));
                }
            }
            Optional<ExperimentStepsResponseDto> steps = simulationService.getExperimentStepsV2(id, shuffle, roleType);
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

    /**
     * 获取所有应急流程列表
     *
     * @return 应急流程列表
     */
    @Operation(summary = "获取应急流程列表", description = "获取所有可用的应急流程信息")
    @GetMapping("/emergency-procedures")
    public ResponseEntity<SimulationApiResponse<List<EmergencyProcedureDto>>> getAllEmergencyProcedures() {
        try {
            List<EmergencyProcedureDto> procedures = simulationService.getAllEmergencyProcedures();
            return ResponseEntity.ok(SimulationApiResponse.success("获取应急流程列表成功", procedures));
        } catch (Exception e) {
            log.error("获取应急流程列表失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SimulationApiResponse.error("获取应急流程列表失败: " + e.getMessage()));
        }
    }

    /**
     * 根据ID获取应急流程详情
     *
     * @param id 应急流程ID
     * @return 应急流程详情
     */
    @Operation(summary = "获取应急流程详情", description = "根据ID获取特定应急流程的详细信息")
    @GetMapping("/emergency-procedures/{id}")
    public ResponseEntity<SimulationApiResponse<EmergencyProcedureDto>> getEmergencyProcedureById(
            @Parameter(description = "应急流程ID") @PathVariable Long id) {
        try {
            Optional<EmergencyProcedureDto> procedure = simulationService.getEmergencyProcedureById(id);
            if (procedure.isPresent()) {
                return ResponseEntity.ok(SimulationApiResponse.success("获取应急流程详情成功", procedure.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(SimulationApiResponse.error("未找到ID为 " + id + " 的应急流程"));
            }
        } catch (Exception e) {
            log.error("获取应急流程详情失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SimulationApiResponse.error("获取应急流程详情失败: " + e.getMessage()));
        }
    }

    /**
     * 根据ID获取试验组件列表
     *
     * @param id 试验组件ID
     * @return 试验组件列表数据
     */
    @Operation(summary = "获取试验组件列表", description = "根据ID获取特定试验组件的列表数据")
    @GetMapping("/components/{id}")
    public ResponseEntity<SimulationApiResponse<ExperimentComponentDto>> getExperimentComponentById(
            @Parameter(description = "试验组件ID") @PathVariable Long id) {
        try {
            Optional<ExperimentComponentDto> component = simulationService.getExperimentComponentById(id);
            if (component.isPresent()) {
                return ResponseEntity.ok(SimulationApiResponse.success("获取试验组件列表成功", component.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(SimulationApiResponse.error("未找到ID为 " + id + " 的试验组件"));
            }
        } catch (Exception e) {
            log.error("获取试验组件列表失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SimulationApiResponse.error("获取试验组件列表失败: " + e.getMessage()));
        }
    }
}