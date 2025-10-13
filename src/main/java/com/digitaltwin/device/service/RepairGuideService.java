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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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
                // 处理新增的deviceId字段
                Object deviceIdObj = guideMap.get("deviceId");
                if (deviceIdObj != null) {
                    dto.setDeviceId(((Number) deviceIdObj).longValue());
                }
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
            dto.setDeviceId(guide.getDeviceId());

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

    /**
     * 根据type获取修复指南列表
     * @param type 指南类型：troubleshooting 或 repair
     * @return 指定类型的指南列表
     */
    public List<RepairGuideDto> getRepairGuidesByType(String type) {
        // 获取当前用户（可选）
        User currentUser = SecurityContext.getCurrentUser();

        final List<RepairGuideLearnStatus> learnStatusList;

        // 如果用户已登录，查询用户学习状态
        if (currentUser != null) {
            learnStatusList = repairGuideLearnStatusRepository.findByUserId(currentUser.getId());
        } else {
            learnStatusList = new ArrayList<>();
        }

        // 按type筛选并设置学习状态
        return repairGuides.stream()
                .filter(guide -> type.equals(guide.getType()))
                .map(guide -> {
                    RepairGuideDto dto = new RepairGuideDto();
                    dto.setId(guide.getId());
                    dto.setName(guide.getName());
                    dto.setType(guide.getType());
                    dto.setContent(guide.getContent());
                    dto.setDeviceId(guide.getDeviceId());

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

    /**
     * 根据type和deviceId获取修复指南列表
     * @param type 指南类型：troubleshooting 或 repair
     * @param deviceId 设备ID（可选，为null时不按设备筛选）
     * @return 指定类型和设备的指南列表
     */
    public List<RepairGuideDto> getRepairGuidesByTypeAndDevice(String type, Long deviceId) {
        // 获取当前用户（可选）
        User currentUser = SecurityContext.getCurrentUser();

        final List<RepairGuideLearnStatus> learnStatusList;

        // 如果用户已登录，查询用户学习状态
        if (currentUser != null) {
            learnStatusList = repairGuideLearnStatusRepository.findByUserId(currentUser.getId());
        } else {
            learnStatusList = new ArrayList<>();
        }

        // 按type和deviceId筛选并设置学习状态
        return repairGuides.stream()
                .filter(guide -> type.equals(guide.getType()))
                .filter(guide -> deviceId == null || deviceId.equals(guide.getDeviceId()))
                .map(guide -> {
                    RepairGuideDto dto = new RepairGuideDto();
                    dto.setId(guide.getId());
                    dto.setName(guide.getName());
                    dto.setType(guide.getType());
                    dto.setContent(guide.getContent());
                    dto.setDeviceId(guide.getDeviceId());

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

    /**
     * 分页获取指定类型的修复指南列表
     * @param type 指南类型：troubleshooting 或 repair
     * @param deviceId 设备ID（可选，为null时不按设备筛选）
     * @param pageable 分页参数
     * @return 分页的指南列表
     */
    public Page<RepairGuideDto> getRepairGuidesByTypeAndDeviceWithPagination(String type, Long deviceId, Pageable pageable) {
        // 先获取全量数据
        List<RepairGuideDto> allGuides = getRepairGuidesByTypeAndDevice(type, deviceId);

        // 计算分页
        int totalElements = allGuides.size();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), totalElements);

        // 提取当前页的数据
        List<RepairGuideDto> pageContent = start >= totalElements ?
            new ArrayList<>() :
            allGuides.subList(start, end);

        return new PageImpl<>(pageContent, pageable, totalElements);
    }
}