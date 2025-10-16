package com.digitaltwin.simulation.controller;

import com.digitaltwin.simulation.dto.SimulationApiResponse;
import com.digitaltwin.simulation.dto.SimulationExperimentListDto;
import com.digitaltwin.simulation.dto.ExperimentStepsDto;
import com.digitaltwin.simulation.dto.ExperimentStepsResponseDto;
import com.digitaltwin.simulation.dto.ExperimentDescriptionDto;
import com.digitaltwin.simulation.dto.SubmitExperimentStepRequest;
import com.digitaltwin.simulation.dto.UpdateExperimentStepsRequest;
import com.digitaltwin.simulation.dto.EmergencyProcedureDto;
import com.digitaltwin.simulation.dto.ExperimentComponentDto;
import com.digitaltwin.simulation.dto.CreateExperimentRequest;
import com.digitaltwin.simulation.dto.CreateExperimentResponse;
import com.digitaltwin.simulation.dto.SimulationStepNode;
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
import javax.validation.Valid;

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
     * 创建新试验
     *
     * @param request 创建试验请求
     * @return 创建的试验信息
     */
    @Operation(summary = "创建新试验", description = "创建一个新的仿真试验，可以选择性地包含试验步骤数据")
    @PostMapping
    public ResponseEntity<SimulationApiResponse<CreateExperimentResponse>> createExperiment(
            @Valid @RequestBody CreateExperimentRequest request) {
        try {
            CreateExperimentResponse response = simulationService.createExperiment(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(SimulationApiResponse.success("创建试验成功", response));
        } catch (IllegalArgumentException e) {
            log.error("创建试验参数错误: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(SimulationApiResponse.error("创建试验失败: " + e.getMessage()));
        } catch (Exception e) {
            log.error("创建试验失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SimulationApiResponse.error("创建试验失败: " + e.getMessage()));
        }
    }

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

    /**
     * 修改试验步骤
     *
     * @param id 试验ID
     * @param request 修改请求，包含完整的步骤数据
     * @return 修改结果
     */
    @Operation(summary = "修改试验步骤", description = "更新指定试验的完整步骤数据，会完全替换原有步骤")
    @PutMapping("/{id}/steps")
    public ResponseEntity<SimulationApiResponse<String>> updateExperimentSteps(
            @Parameter(description = "试验ID") @PathVariable Long id,
            @Valid @RequestBody UpdateExperimentStepsRequest request) {
        try {
            // 验证请求参数
            if (!id.equals(request.getExperimentId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(SimulationApiResponse.error("路径中的试验ID与请求体中的试验ID不匹配"));
            }

            boolean success = simulationService.updateExperimentSteps(request.getExperimentId(), request.getSteps());
            if (success) {
                return ResponseEntity.ok(SimulationApiResponse.success("修改试验步骤成功", "操作完成"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(SimulationApiResponse.error("修改试验步骤失败，请检查数据格式和试验ID"));
            }
        } catch (Exception e) {
            log.error("修改试验步骤失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SimulationApiResponse.error("修改试验步骤失败: " + e.getMessage()));
        }
    }

    /**
     * 删除试验
     *
     * @param id 试验ID
     * @return 删除结果
     */
    @Operation(summary = "删除试验", description = "根据ID删除指定的仿真试验")
    @PostMapping("/{id}/delete")
    public ResponseEntity<SimulationApiResponse<Void>> deleteExperiment(
            @Parameter(description = "试验ID") @PathVariable Long id) {
        try {
            boolean deleted = simulationService.deleteExperiment(id);
            if (deleted) {
                return ResponseEntity.ok(SimulationApiResponse.success("删除试验成功", null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(SimulationApiResponse.error("未找到ID为 " + id + " 的试验"));
            }
        } catch (Exception e) {
            log.error("删除试验失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SimulationApiResponse.error("删除试验失败: " + e.getMessage()));
        }
    }

    /**
     * 获取试验的所有SimulationStepNode
     *
     * @param id 试验ID
     * @param role 角色类型过滤，可选参数
     * @return 所有SimulationStepNode的扁平化列表
     */
    @Operation(summary = "获取试验的所有节点", description = "获取指定试验的所有SimulationStepNode，包括嵌套的子节点")
    @GetMapping("/{id}/nodes")
    public ResponseEntity<SimulationApiResponse<List<SimulationStepNode>>> getExperimentNodes(
            @Parameter(description = "试验ID") @PathVariable Long id,
            @Parameter(description = "角色类型过滤，支持'data_operator'和'commander'，为空时返回全量数据")
            @RequestParam(value = "role", required = false) String role) {
        try {
            RoleType roleType = null;
            if (role != null && !role.trim().isEmpty()) {
                roleType = RoleType.fromDisplayName(role.trim());
                if (roleType == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(SimulationApiResponse.error("不支持的角色类型: " + role + "，支持的角色类型: data_operator, commander"));
                }
            }

            Optional<List<SimulationStepNode>> nodes = simulationService.getExperimentNodes(id, roleType);
            if (nodes.isPresent()) {
                return ResponseEntity.ok(SimulationApiResponse.success("获取试验节点成功", nodes.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(SimulationApiResponse.error("未找到ID为 " + id + " 的试验或试验无节点数据"));
            }
        } catch (Exception e) {
            log.error("获取试验节点失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SimulationApiResponse.error("获取试验节点失败: " + e.getMessage()));
        }
    }
}