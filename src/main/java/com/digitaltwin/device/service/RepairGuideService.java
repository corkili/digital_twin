package com.digitaltwin.device.service;

import com.digitaltwin.device.entity.RepairGuideLearnStatus;
import com.digitaltwin.device.repository.RepairGuideLearnStatusRepository;
import com.digitaltwin.device.dto.RepairGuideDto;
import com.digitaltwin.system.entity.User;
import com.digitaltwin.system.util.SecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
public class RepairGuideService {

    @Autowired
    private RepairGuideLearnStatusRepository repairGuideLearnStatusRepository;

    // 存储从JSON文件读取的指南数据
    private List<RepairGuideDto> repairGuides = new ArrayList<>();

    @PostConstruct
    public void init() {
        loadRepairGuidesFromJson();
    }

    private void loadRepairGuidesFromJson() {
        try {
            // 从resources目录读取JSON文件
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("repair_guides.json");
            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, Object>> guideMaps = objectMapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>() {});

            // 转换为RepairGuideDto对象
            for (Map<String, Object> guideMap : guideMaps) {
                RepairGuideDto dto = new RepairGuideDto();
                dto.setId(((Number) guideMap.get("id")).longValue());
                dto.setName((String) guideMap.get("name"));
                dto.setType((String) guideMap.get("type"));
                dto.setContent((String) guideMap.get("content"));
                repairGuides.add(dto);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<RepairGuideDto> getRepairGuides() {
        // 获取当前用户（可选）
        User currentUser = SecurityContext.getCurrentUser();

        final List<RepairGuideLearnStatus> learnStatusList;

        // 如果用户已登录，查询用户学习状态
        if (currentUser != null) {
            learnStatusList = repairGuideLearnStatusRepository.findByUserId(currentUser.getId());
        } else {
            learnStatusList = new ArrayList<>();
        }

        // 设置学习状态并返回指南列表
        return repairGuides.stream().map(guide -> {
            RepairGuideDto dto = new RepairGuideDto();
            dto.setId(guide.getId());
            dto.setName(guide.getName());
            dto.setType(guide.getType());
            dto.setContent(guide.getContent());

            // 设置学习状态（未登录时默认为false）
            boolean isLearned = false;
            if (currentUser != null) {
                isLearned = learnStatusList.stream()
                        .anyMatch(status -> status.getGuideId().equals(guide.getId()) && status.getIsLearned());
            }
            dto.setIsLearned(isLearned);

            return dto;
        }).collect(Collectors.toList());
    }

    public void learnRepairGuide(Long guideId) {
        // 获取当前用户
        User currentUser = SecurityContext.getCurrentUser();
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }

        // 验证指南ID是否存在
        Optional<RepairGuideDto> guideExists = repairGuides.stream()
                .filter(guide -> guide.getId().equals(guideId))
                .findFirst();
        
        if (!guideExists.isPresent()) {
            throw new RuntimeException("指定的指南不存在");
        }

        // 查找或创建学习状态记录
        RepairGuideLearnStatus learnStatus = repairGuideLearnStatusRepository
                .findByGuideIdAndUserId(guideId, currentUser.getId())
                .orElse(new RepairGuideLearnStatus());

        // 更新学习状态
        learnStatus.setGuideId(guideId);
        learnStatus.setUserId(currentUser.getId());
        learnStatus.setIsLearned(true);
        learnStatus.setLearnedAt(java.time.LocalDateTime.now());

        // 保存学习状态
        repairGuideLearnStatusRepository.save(learnStatus);
    }
}