package com.digitaltwin.simulation.controller;

import com.digitaltwin.device.dto.ApiResponse;
import com.digitaltwin.simulation.dto.VirtualSceneDetailDto;
import com.digitaltwin.simulation.service.VirtualSceneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * 虚拟场景控制器
 * 专门处理虚拟场景相关的REST API接口
 */
@Slf4j
@RestController
@RequestMapping("/virtual-scenes")
@RequiredArgsConstructor
public class VirtualSceneController {
    
    private final VirtualSceneService virtualSceneService;
    
    /**
     * 根据场景ID获取虚拟场景详情
     * 
     * @param id 场景ID
     * @return 场景详情和组件结构
     */
    @GetMapping("/{id}/detail")
    public ResponseEntity<ApiResponse> getVirtualSceneDetail(@PathVariable Long id) {
        try {
            Optional<VirtualSceneDetailDto> detail = virtualSceneService.getVirtualSceneDetail(id);
            if (detail.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("获取虚拟场景详情成功", detail.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("未找到ID为 " + id + " 的虚拟场景"));
            }
        } catch (Exception e) {
            log.error("获取虚拟场景详情失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("获取虚拟场景详情失败: " + e.getMessage()));
        }
    }
}