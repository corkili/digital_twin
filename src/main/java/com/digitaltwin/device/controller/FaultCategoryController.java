package com.digitaltwin.device.controller;

import com.digitaltwin.device.dto.ApiResponse;
import com.digitaltwin.device.dto.device.FaultCategoryDto;
import com.digitaltwin.device.service.FaultCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/fault-categories")
@RequiredArgsConstructor
@Tag(name = "故障分类管理", description = "提供故障分类查询接口")
public class FaultCategoryController {

    private final FaultCategoryService faultCategoryService;

    /**
     * 获取所有故障分类列表
     */
    @Operation(summary = "获取故障分类列表", description = "获取所有故障分类信息，按训练难度排序")
    @GetMapping
    public ResponseEntity<ApiResponse> getAllFaultCategories() {
        try {
            List<FaultCategoryDto> faultCategories = faultCategoryService.getAllFaultCategories();
            return ResponseEntity.ok(ApiResponse.success("查询故障分类列表成功", faultCategories));
        } catch (Exception e) {
            log.error("查询故障分类列表失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("查询故障分类列表失败: " + e.getMessage()));
        }
    }

    /**
     * 根据故障类型模糊查询故障分类
     */
    @Operation(summary = "根据故障类型查询", description = "根据故障类型关键词模糊查询故障分类")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse> getFaultCategoriesByType(@RequestParam String faultType) {
        try {
            List<FaultCategoryDto> faultCategories = faultCategoryService.getFaultCategoriesByType(faultType);
            return ResponseEntity.ok(ApiResponse.success("查询故障分类成功", faultCategories));
        } catch (Exception e) {
            log.error("根据故障类型查询故障分类失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("根据故障类型查询故障分类失败: " + e.getMessage()));
        }
    }

    /**
     * 根据训练难度查询故障分类
     */
    @Operation(summary = "根据训练难度查询", description = "根据训练难度查询故障分类")
    @GetMapping("/difficulty/{trainingDifficulty}")
    public ResponseEntity<ApiResponse> getFaultCategoriesByDifficulty(@PathVariable Integer trainingDifficulty) {
        try {
            List<FaultCategoryDto> faultCategories = faultCategoryService.getFaultCategoriesByDifficulty(trainingDifficulty);
            return ResponseEntity.ok(ApiResponse.success("根据训练难度查询故障分类成功", faultCategories));
        } catch (Exception e) {
            log.error("根据训练难度查询故障分类失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("根据训练难度查询故障分类失败: " + e.getMessage()));
        }
    }

    /**
     * 根据训练难度范围查询故障分类
     */
    @Operation(summary = "根据训练难度范围查询", description = "根据训练难度范围查询故障分类")
    @GetMapping("/difficulty-range")
    public ResponseEntity<ApiResponse> getFaultCategoriesByDifficultyRange(
            @RequestParam Integer minDifficulty,
            @RequestParam Integer maxDifficulty) {
        try {
            List<FaultCategoryDto> faultCategories = faultCategoryService.getFaultCategoriesByDifficultyRange(minDifficulty, maxDifficulty);
            return ResponseEntity.ok(ApiResponse.success("根据训练难度范围查询故障分类成功", faultCategories));
        } catch (Exception e) {
            log.error("根据训练难度范围查询故障分类失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("根据训练难度范围查询故障分类失败: " + e.getMessage()));
        }
    }
}