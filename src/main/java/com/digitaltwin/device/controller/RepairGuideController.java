package com.digitaltwin.device.controller;

import com.digitaltwin.device.dto.RepairGuideDto;
import com.digitaltwin.device.dto.LearnRepairGuideRequest;
import com.digitaltwin.device.service.RepairGuideService;
import com.digitaltwin.device.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/device/repair-guides")
@Tag(name = "设备故障修复指南", description = "提供设备故障修复指南的管理接口，包括获取指南列表和学习状态更新")
public class RepairGuideController {

    @Autowired
 private RepairGuideService repairGuideService;

    /**
     * 获取设备故障修复指南列表
     * @return 指南列表，按类型分类。如果用户已登录，返回个人学习状态；如果未登录，所有isLearned字段为false
     */
    @Operation(summary = "获取修复指南列表", description = "获取所有设备故障修复指南，按类型分组返回。支持未登录访问，登录用户可查看学习状态")
    @GetMapping
    public ResponseEntity<ApiResponse> getRepairGuides() {
        try {
            List<RepairGuideDto> guides = repairGuideService.getRepairGuides();

            // 按类型分组
            Map<String, List<RepairGuideDto>> groupedGuides = guides.stream()
                    .collect(Collectors.groupingBy(RepairGuideDto::getType));

            return ResponseEntity.ok(ApiResponse.success(groupedGuides));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error("获取指南列表失败: " + e.getMessage()));
        }
    }

    /**
     * 学习修复指南接口
     * @param request 指南ID请求
     * @return 操作结果
     */
    @Operation(summary = "学习修复指南", description = "标记用户已学习指定的修复指南，更新学习状态")
    @PostMapping("/learn")
    public ResponseEntity<ApiResponse> learnRepairGuide(@Valid @RequestBody LearnRepairGuideRequest request) {
        try {
            repairGuideService.learnRepairGuide(request.getGuideId());
            return ResponseEntity.ok(ApiResponse.success("学习状态更新成功"));
        } catch (Exception e) {
            if ("用户未登录".equals(e.getMessage())) {
                return ResponseEntity.status(401).body(ApiResponse.error("用户未登录"));
            }
            return ResponseEntity.ok(ApiResponse.error("学习指南失败: " + e.getMessage()));
        }
    }
}