package com.digitaltwin.simulation.controller;

import com.digitaltwin.simulation.dto.*;
import com.digitaltwin.simulation.service.KnowledgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 知识库控制器
 * 提供知识库的CRUD操作接口
 */
@Slf4j
@RestController
@RequestMapping("/knowledge")
@RequiredArgsConstructor
@Tag(name = "知识库管理", description = "提供知识库的CRUD操作接口，支持分页查询和全文搜索")
public class KnowledgeController {
    
    private final KnowledgeService knowledgeService;
    
    /**
     * 创建知识库
     * 
     * @param request 创建请求
     * @return 创建结果
     */
    @PostMapping
    public ResponseEntity<SimulationApiResponse<KnowledgeDto>> createKnowledge(@RequestBody CreateKnowledgeRequest request) {
        try {
            KnowledgeDto knowledge = knowledgeService.createKnowledge(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(SimulationApiResponse.success("创建知识库成功", knowledge));
        } catch (Exception e) {
            log.error("创建知识库失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SimulationApiResponse.error("创建知识库失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取知识库列表（支持分页）
     * 
     * @param page 页码（从0开始，默认0）
     * @param size 每页数量（默认10）
     * @param sortBy 排序字段（默认createdAt）
     * @param sortDir 排序方向（asc/desc，默认desc）
     * @return 知识库列表
     */
    @GetMapping
    public ResponseEntity<SimulationApiResponse<KnowledgeListResponse>> getAllKnowledge(
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量，设为0返回全部") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向 asc/desc") @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            // 如果不传分页参数或size为0，返回全部数据
            if (size == 0) {
                List<KnowledgeDto> knowledgeList = knowledgeService.getAllKnowledge();
                KnowledgeListResponse response = KnowledgeListResponse.fromList(knowledgeList);
                return ResponseEntity.ok(SimulationApiResponse.success("获取知识库列表成功", response));
            }
            
            Page<KnowledgeDto> knowledgePage = knowledgeService.getKnowledgeWithPagination(page, size, sortBy, sortDir);
            KnowledgeListResponse response = KnowledgeListResponse.fromPage(knowledgePage);
            return ResponseEntity.ok(SimulationApiResponse.success("获取知识库列表成功", response));
        } catch (Exception e) {
            log.error("获取知识库列表失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SimulationApiResponse.error("获取知识库列表失败: " + e.getMessage()));
        }
    }
    
    /**
     * 根据ID获取知识库详情
     * 
     * @param id 知识库ID
     * @return 知识库详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<SimulationApiResponse<KnowledgeDto>> getKnowledgeById(
            @Parameter(description = "知识库ID") @PathVariable Long id) {
        try {
            Optional<KnowledgeDto> knowledge = knowledgeService.getKnowledgeById(id);
            if (knowledge.isPresent()) {
                return ResponseEntity.ok(SimulationApiResponse.success("获取知识库详情成功", knowledge.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(SimulationApiResponse.error("未找到ID为 " + id + " 的知识库"));
            }
        } catch (Exception e) {
            log.error("获取知识库详情失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SimulationApiResponse.error("获取知识库详情失败: " + e.getMessage()));
        }
    }


    /**
     * 更新知识库
     * 
     * @param id 知识库ID
     * @param request 更新请求
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public ResponseEntity<SimulationApiResponse<KnowledgeDto>> updateKnowledge(
            @Parameter(description = "知识库ID") @PathVariable Long id, 
            @RequestBody UpdateKnowledgeRequest request) {
        try {
            Optional<KnowledgeDto> knowledge = knowledgeService.updateKnowledge(id, request);
            if (knowledge.isPresent()) {
                return ResponseEntity.ok(SimulationApiResponse.success("更新知识库成功", knowledge.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(SimulationApiResponse.error("未找到ID为 " + id + " 的知识库"));
            }
        } catch (Exception e) {
            log.error("更新知识库失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SimulationApiResponse.error("更新知识库失败: " + e.getMessage()));
        }
    }
    
    /**
     * 删除知识库
     * 
     * @param id 知识库ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<SimulationApiResponse<Void>> deleteKnowledge(
            @Parameter(description = "知识库ID") @PathVariable Long id) {
        try {
            boolean deleted = knowledgeService.deleteKnowledge(id);
            if (deleted) {
                return ResponseEntity.ok(SimulationApiResponse.success("删除知识库成功", null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(SimulationApiResponse.error("未找到ID为 " + id + " 的知识库"));
            }
        } catch (Exception e) {
            log.error("删除知识库失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SimulationApiResponse.error("删除知识库失败: " + e.getMessage()));
        }
    }
    
    /**
     * 全文搜索知识库
     * 搜索标题和目录名称
     *
     * @param keyword 搜索关键字
     * @param page 页码，从0开始
     * @param size 每页数量，设为0返回全部
     * @param sortBy 排序字段
     * @param sortDir 排序方向 asc/desc
     * @return 搜索结果列表（支持分页）
     */
    @Operation(summary = "全文搜索知识库", description = "支持分页；当size=0时返回全部；当keyword为空时返回所有数据")
    @GetMapping("/search")
    public ResponseEntity<SimulationApiResponse<KnowledgeListResponse>> fullTextSearch(
            @Parameter(description = "搜索关键字", required = true) @RequestParam String keyword,
            @Parameter(description = "页码，从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量，设为0返回全部") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "排序字段") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "排序方向 asc/desc") @RequestParam(defaultValue = "desc") String sortDir) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                if (size == 0) {
                    List<KnowledgeDto> knowledgeList = knowledgeService.getAllKnowledge();
                    KnowledgeListResponse response = KnowledgeListResponse.fromList(knowledgeList);
                    return ResponseEntity.ok(SimulationApiResponse.success("查询成功", response));
                }
                Page<KnowledgeDto> pageData = knowledgeService.getKnowledgeWithPagination(page, size, sortBy, sortDir);
                KnowledgeListResponse response = KnowledgeListResponse.fromPage(pageData);
                return ResponseEntity.ok(SimulationApiResponse.success("查询成功", response));
            }
            
            if (size == 0) {
                List<KnowledgeDto> knowledgeList = knowledgeService.fullTextSearch(keyword);
                KnowledgeListResponse response = KnowledgeListResponse.fromList(knowledgeList);
                return ResponseEntity.ok(SimulationApiResponse.success("搜索成功", response));
            }
            Page<KnowledgeDto> pageData = knowledgeService.fullTextSearchWithPagination(keyword, page, size, sortBy, sortDir);
            KnowledgeListResponse response = KnowledgeListResponse.fromPage(pageData);
            return ResponseEntity.ok(SimulationApiResponse.success("搜索成功", response));
        } catch (Exception e) {
            log.error("全文搜索知识库失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SimulationApiResponse.error("全文搜索知识库失败: " + e.getMessage()));
        }
    }
}
