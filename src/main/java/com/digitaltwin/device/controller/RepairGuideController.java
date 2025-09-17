package com.digitaltwin.device.controller;

import com.digitaltwin.device.dto.RepairGuideDto;
import com.digitaltwin.device.dto.LearnRepairGuideRequest;
import com.digitaltwin.device.service.RepairGuideService;
import com.digitaltwin.device.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/device/repair-guides")
public class RepairGuideController {

    @Autowired
 private RepairGuideService repairGuideService;

    /**
     * 获取设备故障修复指南列表
     * @return 指南列表，按类型分类
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getRepairGuides() {
        try {
            List<RepairGuideDto> guides = repairGuideService.getRepairGuides();
            
            // 按类型分组
            Map<String, List<RepairGuideDto>> groupedGuides = guides.stream()
                    .collect(Collectors.groupingBy(RepairGuideDto::getType));
            
            return ResponseEntity.ok(ApiResponse.success(groupedGuides));
        } catch (Exception e) {
            if ("用户未登录".equals(e.getMessage())) {
                return ResponseEntity.status(401).body(ApiResponse.error("用户未登录"));
            }
            return ResponseEntity.ok(ApiResponse.error("获取指南列表失败: " + e.getMessage()));
        }
    }

    /**
     * 学习修复指南接口
     * @param request 指南ID请求
     * @return 操作结果
     */
    @PostMapping("/learn")
    public ResponseEntity<ApiResponse> learnRepairGuide(@RequestBody LearnRepairGuideRequest request) {
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