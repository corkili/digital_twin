package com.digitaltwin.simulation.controller;

import com.digitaltwin.simulation.dto.*;
import com.digitaltwin.simulation.service.ExamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/exams")
@RequiredArgsConstructor
@Tag(name = "评定考核管理", description = "提供评定考核的CRUD与分页查询")
public class ExamController {

    private final ExamService examService;

    @Operation(summary = "创建记录")
    @PostMapping
    public ResponseEntity<SimulationApiResponse<ExamDto>> create(@RequestBody CreateExamRequest request) {
        try {
            ExamDto dto = examService.createExam(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(SimulationApiResponse.success("创建成功", dto));
        } catch (Exception e) {
            log.error("创建Exam失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SimulationApiResponse.error("创建失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "获取记录详情")
    @GetMapping("/{id}")
    public ResponseEntity<SimulationApiResponse<ExamDto>> getById(@PathVariable Long id) {
        try {
            Optional<ExamDto> dto = examService.getById(id);
            return dto
                    .map(value -> ResponseEntity.ok(SimulationApiResponse.success("查询成功", value)))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(SimulationApiResponse.error("未找到ID为 " + id + " 的记录")));
        } catch (Exception e) {
            log.error("查询Exam失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SimulationApiResponse.error("查询失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "获取记录列表（支持分页）")
    @GetMapping
    public ResponseEntity<SimulationApiResponse<ExamListResponse>> list(
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量，设为0返回全部") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向 asc/desc") @RequestParam(defaultValue = "desc") String sortDir
    ) {
        try {
            if (size == 0) {
                List<ExamDto> list = examService.getAll();
                return ResponseEntity.ok(SimulationApiResponse.success("查询成功", ExamListResponse.fromList(list)));
            }
            Page<ExamDto> pageData = examService.getPage(page, size, sortBy, sortDir);
            return ResponseEntity.ok(SimulationApiResponse.success("查询成功", ExamListResponse.fromPage(pageData)));
        } catch (Exception e) {
            log.error("分页查询Exam失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SimulationApiResponse.error("查询失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "更新记录")
    @PostMapping("/{id}/update")
    public ResponseEntity<SimulationApiResponse<ExamDto>> update(
            @PathVariable Long id,
            @RequestBody UpdateExamRequest request) {
        try {
            Optional<ExamDto> updated = examService.update(id, request);
            return updated
                    .map(value -> ResponseEntity.ok(SimulationApiResponse.success("更新成功", value)))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(SimulationApiResponse.error("未找到ID为 " + id + " 的记录")));
        } catch (Exception e) {
            log.error("更新Exam失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SimulationApiResponse.error("更新失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "删除记录")
    @PostMapping("/{id}/delete")
    public ResponseEntity<SimulationApiResponse<Void>> delete(@PathVariable Long id) {
        try {
            boolean ok = examService.delete(id);
            if (ok) {
                return ResponseEntity.ok(SimulationApiResponse.success("删除成功", null));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(SimulationApiResponse.error("未找到ID为 " + id + " 的记录"));
        } catch (Exception e) {
            log.error("删除Exam失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SimulationApiResponse.error("删除失败: " + e.getMessage()));
        }
    }

    @Operation(summary = "按姓名模糊搜索", description = "支持分页；当size=0时返回全部")
    @GetMapping("/search")
    public ResponseEntity<SimulationApiResponse<ExamListResponse>> searchByName(
            @Parameter(description = "姓名关键字", required = true) @RequestParam String name,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量，设为0返回全部") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向 asc/desc") @RequestParam(defaultValue = "desc") String sortDir
    ) {
        try {
            if (size == 0) {
                List<ExamDto> list = examService.searchByName(name);
                return ResponseEntity.ok(SimulationApiResponse.success("搜索成功", ExamListResponse.fromList(list)));
            }
            Page<ExamDto> pageData = examService.searchByNameWithPagination(name, page, size, sortBy, sortDir);
            return ResponseEntity.ok(SimulationApiResponse.success("搜索成功", ExamListResponse.fromPage(pageData)));
        } catch (Exception e) {
            log.error("按姓名搜索Exam失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SimulationApiResponse.error("搜索失败: " + e.getMessage()));
        }
    }
}
