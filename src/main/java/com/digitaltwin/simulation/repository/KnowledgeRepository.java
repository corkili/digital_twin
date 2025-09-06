package com.digitaltwin.simulation.repository;

import com.digitaltwin.simulation.entity.Knowledge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 知识库Repository
 */
@Repository
public interface KnowledgeRepository extends JpaRepository<Knowledge, Long> {
    
    /**
     * 根据状态查询知识库
     * @param status 状态
     * @return 知识库列表
     */
    List<Knowledge> findByStatus(String status);
    
    /**
     * 根据标题查询知识库
     * @param title 标题
     * @return 知识库
     */
    Optional<Knowledge> findByTitle(String title);
    
    /**
     * 检查标题是否存在
     * @param title 标题
     * @return 是否存在
     */
    boolean existsByTitle(String title);
    
    /**
     * 根据标题模糊查询
     * @param title 标题关键字
     * @return 知识库列表
     */
    List<Knowledge> findByTitleContaining(String title);
    
    /**
     * 全文搜索：搜索标题和目录名称
     * @param keyword 搜索关键字
     * @return 知识库列表
     */
    @Query(value = "SELECT DISTINCT * FROM knowledge " +
           "WHERE status = 'ACTIVE' AND (" +
           "title LIKE CONCAT('%', :keyword, '%') OR " +
           "JSON_SEARCH(catalog_data, 'all', CONCAT('%', :keyword, '%')) IS NOT NULL" +
           ") ORDER BY created_at DESC", 
           nativeQuery = true)
    List<Knowledge> fullTextSearch(@Param("keyword") String keyword);
    
    /**
     * 分页全文搜索：搜索标题和目录名称
     * @param keyword 搜索关键字
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Query(value = "SELECT DISTINCT * FROM knowledge " +
           "WHERE status = 'ACTIVE' AND (" +
           "title LIKE CONCAT('%', :keyword, '%') OR " +
           "JSON_SEARCH(catalog_data, 'all', CONCAT('%', :keyword, '%')) IS NOT NULL" +
           ")", 
           nativeQuery = true)
    Page<Knowledge> fullTextSearchWithPagination(@Param("keyword") String keyword, Pageable pageable);
}