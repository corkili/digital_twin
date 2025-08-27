package com.digitaltwin.device.controller;

import com.digitaltwin.device.dto.ApiResponse;
import com.digitaltwin.device.dto.device.CreatePointRequest;
import com.digitaltwin.device.dto.device.PointDto;
import com.digitaltwin.device.dto.device.UpdatePointRequest;
import com.digitaltwin.device.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/points")
@RequiredArgsConstructor
public class PointManagementController {
    
    private final PointService pointService;
    
    /**
     * 创建点位
     * @param request 创建点位请求
     * @return 点位信息
     */
    @PostMapping
    public ResponseEntity<ApiResponse> createPoint(@RequestBody CreatePointRequest request) {
        try {
            PointDto pointDto = pointService.createPoint(request);
            return ResponseEntity.ok(ApiResponse.success("Point created successfully", pointDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to create point: " + e.getMessage()));
        }
    }
    
    /**
     * 根据ID获取点位
     * @param id 点位ID
     * @return 点位信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getPointById(@PathVariable Long id) {
        try {
            PointDto pointDto = pointService.getPointById(id);
            return ResponseEntity.ok(ApiResponse.success("Point retrieved successfully", pointDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve point: " + e.getMessage()));
        }
    }
    
    /**
     * 根据标识获取点位
     * @param identity 点位标识
     * @return 点位信息
     */
    @GetMapping("/identity/{identity}")
    public ResponseEntity<ApiResponse> getPointByIdentity(@PathVariable String identity) {
        try {
            PointDto pointDto = pointService.getPointByIdentity(identity);
            return ResponseEntity.ok(ApiResponse.success("Point retrieved successfully", pointDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve point: " + e.getMessage()));
        }
    }
    
    /**
     * 获取所有点位
     * @return 点位列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getAllPoints() {
        try {
            List<PointDto> points = pointService.getAllPoints();
            return ResponseEntity.ok(ApiResponse.success("Points retrieved successfully", points));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to retrieve points: " + e.getMessage()));
        }
    }
    
    /**
     * 更新点位
     * @param id 点位ID
     * @param request 更新点位请求
     * @return 点位信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updatePoint(@PathVariable Long id, @RequestBody UpdatePointRequest request) {
        try {
            PointDto pointDto = pointService.updatePoint(id, request);
            return ResponseEntity.ok(ApiResponse.success("Point updated successfully", pointDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to update point: " + e.getMessage()));
        }
    }
    
    /**
     * 删除点位
     * @param id 点位ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deletePoint(@PathVariable Long id) {
        try {
            pointService.deletePoint(id);
            return ResponseEntity.ok(ApiResponse.success("Point deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete point: " + e.getMessage()));
        }
    }
}