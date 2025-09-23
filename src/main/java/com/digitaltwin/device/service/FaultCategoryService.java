package com.digitaltwin.device.service;

import com.digitaltwin.device.dto.device.FaultCategoryDto;
import com.digitaltwin.device.entity.FaultCategory;
import com.digitaltwin.device.repository.FaultCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FaultCategoryService {

    private final FaultCategoryRepository faultCategoryRepository;

    /**
     * 获取所有故障分类列表（按训练难度排序）
     * @return 故障分类列表
     */
    public List<FaultCategoryDto> getAllFaultCategories() {
        try {
            List<FaultCategory> faultCategories = faultCategoryRepository.findAllByOrderByTrainingDifficultyAsc();
            return faultCategories.stream()
                    .map(FaultCategoryDto::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("查询故障分类列表失败: {}", e.getMessage(), e);
            throw new RuntimeException("查询故障分类列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据故障类型模糊查询故障分类
     * @param faultType 故障类型关键词
     * @return 故障分类列表
     */
    public List<FaultCategoryDto> getFaultCategoriesByType(String faultType) {
        try {
            List<FaultCategory> faultCategories = faultCategoryRepository.findByFaultTypeContaining(faultType);
            return faultCategories.stream()
                    .map(FaultCategoryDto::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("根据故障类型查询故障分类失败: {}", e.getMessage(), e);
            throw new RuntimeException("根据故障类型查询故障分类失败: " + e.getMessage());
        }
    }

    /**
     * 根据训练难度查询故障分类
     * @param trainingDifficulty 训练难度
     * @return 故障分类列表
     */
    public List<FaultCategoryDto> getFaultCategoriesByDifficulty(Integer trainingDifficulty) {
        try {
            List<FaultCategory> faultCategories = faultCategoryRepository.findByTrainingDifficulty(trainingDifficulty);
            return faultCategories.stream()
                    .map(FaultCategoryDto::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("根据训练难度查询故障分类失败: {}", e.getMessage(), e);
            throw new RuntimeException("根据训练难度查询故障分类失败: " + e.getMessage());
        }
    }

    /**
     * 根据训练难度范围查询故障分类
     * @param minDifficulty 最小训练难度
     * @param maxDifficulty 最大训练难度
     * @return 故障分类列表
     */
    public List<FaultCategoryDto> getFaultCategoriesByDifficultyRange(Integer minDifficulty, Integer maxDifficulty) {
        try {
            List<FaultCategory> faultCategories = faultCategoryRepository.findByTrainingDifficultyBetween(minDifficulty, maxDifficulty);
            return faultCategories.stream()
                    .map(FaultCategoryDto::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("根据训练难度范围查询故障分类失败: {}", e.getMessage(), e);
            throw new RuntimeException("根据训练难度范围查询故障分类失败: " + e.getMessage());
        }
    }
}