package com.digitaltwin.simulation.service;

import com.digitaltwin.simulation.dto.*;
import com.digitaltwin.simulation.entity.Knowledge;
import com.digitaltwin.simulation.repository.KnowledgeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 知识库服务层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeService {
    
    private final KnowledgeRepository knowledgeRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * 创建知识库
     * @param request 创建请求
     * @return 创建的知识库DTO
     */
    public KnowledgeDto createKnowledge(CreateKnowledgeRequest request) {
        try {
            // 检查标题是否已存在
            if (knowledgeRepository.existsByTitle(request.getTitle())) {
                throw new RuntimeException("标题已存在: " + request.getTitle());
            }
            
            Knowledge knowledge = new Knowledge();
            knowledge.setTitle(request.getTitle());
            
            // 序列化目录数据
            if (request.getCatalog() != null) {
                String catalogJson = objectMapper.writeValueAsString(request.getCatalog());
                knowledge.setCatalogData(catalogJson);
            }
            
            Knowledge savedKnowledge = knowledgeRepository.save(knowledge);
            return convertToDto(savedKnowledge);
        } catch (Exception e) {
            log.error("创建知识库失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建知识库失败", e);
        }
    }
    
    /**
     * 根据ID获取知识库详情
     * @param id 知识库ID
     * @return 知识库详情
     */
    public Optional<KnowledgeDto> getKnowledgeById(Long id) {
        try {
            Optional<Knowledge> knowledge = knowledgeRepository.findById(id);
            return knowledge.map(this::convertToDto);
        } catch (Exception e) {
            log.error("根据ID获取知识库失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取知识库失败", e);
        }
    }
    
    /**
     * 获取所有知识库列表
     * @return 知识库列表（包含目录信息）
     */
    public List<KnowledgeDto> getAllKnowledge() {
        try {
            List<Knowledge> knowledgeList = knowledgeRepository.findAll();
            return knowledgeList.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取知识库列表失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取知识库列表失败", e);
        }
    }
    
    /**
     * 分页获取知识库列表
     * @param page 页码（从0开始）
     * @param size 每页数量
     * @param sortBy 排序字段
     * @param sortDir 排序方向（asc/desc）
     * @return 分页结果
     */
    public Page<KnowledgeDto> getKnowledgeWithPagination(int page, int size, String sortBy, String sortDir) {
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Knowledge> knowledgePage = knowledgeRepository.findAll(pageable);
            return knowledgePage.map(this::convertToDto);
        } catch (Exception e) {
            log.error("分页获取知识库列表失败: {}", e.getMessage(), e);
            throw new RuntimeException("分页获取知识库列表失败", e);
        }
    }
    
    /**
     * 根据状态获取知识库列表
     * @param status 状态
     * @return 知识库列表
     */
    public List<KnowledgeListDto> getKnowledgeByStatus(String status) {
        try {
            List<Knowledge> knowledgeList = knowledgeRepository.findByStatus(status);
            return knowledgeList.stream()
                    .map(KnowledgeListDto::fromEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("根据状态获取知识库列表失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取知识库列表失败", e);
        }
    }
    
    /**
     * 更新知识库
     * @param id 知识库ID
     * @param request 更新请求
     * @return 更新后的知识库DTO
     */
    public Optional<KnowledgeDto> updateKnowledge(Long id, UpdateKnowledgeRequest request) {
        try {
            Optional<Knowledge> existingKnowledge = knowledgeRepository.findById(id);
            if (!existingKnowledge.isPresent()) {
                return Optional.empty();
            }
            
            Knowledge knowledge = existingKnowledge.get();
            
            // 更新标题（检查重复）
            if (request.getTitle() != null && !request.getTitle().equals(knowledge.getTitle())) {
                if (knowledgeRepository.existsByTitle(request.getTitle())) {
                    throw new RuntimeException("标题已存在: " + request.getTitle());
                }
                knowledge.setTitle(request.getTitle());
            }
            
            // 更新目录数据
            if (request.getCatalog() != null) {
                String catalogJson = objectMapper.writeValueAsString(request.getCatalog());
                knowledge.setCatalogData(catalogJson);
            }
            
            // 更新状态
            if (request.getStatus() != null) {
                knowledge.setStatus(request.getStatus());
            }
            
            Knowledge updatedKnowledge = knowledgeRepository.save(knowledge);
            return Optional.of(convertToDto(updatedKnowledge));
        } catch (Exception e) {
            log.error("更新知识库失败: {}", e.getMessage(), e);
            throw new RuntimeException("更新知识库失败", e);
        }
    }
    
    /**
     * 删除知识库
     * @param id 知识库ID
     * @return 是否删除成功
     */
    public boolean deleteKnowledge(Long id) {
        try {
            if (!knowledgeRepository.existsById(id)) {
                return false;
            }
            knowledgeRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            log.error("删除知识库失败: {}", e.getMessage(), e);
            throw new RuntimeException("删除知识库失败", e);
        }
    }
    
    /**
     * 根据标题搜索知识库
     * @param title 标题关键字
     * @return 知识库列表
     */
    public List<KnowledgeListDto> searchKnowledgeByTitle(String title) {
        try {
            List<Knowledge> knowledgeList = knowledgeRepository.findByTitleContaining(title);
            return knowledgeList.stream()
                    .map(KnowledgeListDto::fromEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("搜索知识库失败: {}", e.getMessage(), e);
            throw new RuntimeException("搜索知识库失败", e);
        }
    }
    
    /**
     * 全文搜索知识库：搜索标题和目录名称
     * @param keyword 搜索关键字
     * @return 知识库列表（包含完整目录信息）
     */
    public List<KnowledgeDto> fullTextSearch(String keyword) {
        try {
            List<Knowledge> knowledgeList = knowledgeRepository.fullTextSearch(keyword);
            return knowledgeList.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("全文搜索知识库失败: {}", e.getMessage(), e);
            throw new RuntimeException("全文搜索知识库失败", e);
        }
    }
    
    /**
     * 分页全文搜索知识库：搜索标题和目录名称
     * @param keyword 搜索关键字
     * @param page 页码（从0开始）
     * @param size 每页数量
     * @param sortBy 排序字段
     * @param sortDir 排序方向（asc/desc）
     * @return 分页搜索结果
     */
    public Page<KnowledgeDto> fullTextSearchWithPagination(String keyword, int page, int size, String sortBy, String sortDir) {
        try {
            // 将Java字段名转换为数据库列名
            String dbColumnName = convertToDbColumnName(sortBy);
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), dbColumnName);
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<Knowledge> knowledgePage = knowledgeRepository.fullTextSearchWithPagination(keyword, pageable);
            return knowledgePage.map(this::convertToDto);
        } catch (Exception e) {
            log.error("分页全文搜索知识库失败: {}", e.getMessage(), e);
            throw new RuntimeException("分页全文搜索知识库失败", e);
        }
    }

    /**
     * 将Java字段名转换为数据库列名
     * @param javaFieldName Java字段名
     * @return 数据库列名
     */
    private String convertToDbColumnName(String javaFieldName) {
        switch (javaFieldName) {
            case "createdAt":
                return "created_at";
            case "catalogData":
                return "catalog_data";
            default:
                return javaFieldName;
        }
    }
    
    /**
     * 将实体类转换为DTO
     * @param knowledge 实体类
     * @return DTO
     */
    private KnowledgeDto convertToDto(Knowledge knowledge) {
        try {
            KnowledgeDto dto = KnowledgeDto.fromEntity(knowledge);
            
            // 解析目录数据
            if (knowledge.getCatalogData() != null) {
                CatalogItem[] catalogArray = objectMapper.readValue(
                    knowledge.getCatalogData(), CatalogItem[].class);
                dto.setCatalog(List.of(catalogArray));
            }
            
            return dto;
        } catch (Exception e) {
            log.error("转换DTO失败: {}", e.getMessage(), e);
            throw new RuntimeException("转换DTO失败", e);
        }
    }
}