package com.digitaltwin.simulation.service;

import com.digitaltwin.simulation.dto.VirtualSceneDetailDto;
import com.digitaltwin.simulation.dto.ComponentNode;
import com.digitaltwin.simulation.entity.VirtualScene;
import com.digitaltwin.simulation.repository.VirtualSceneRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 虚拟场景服务层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VirtualSceneService {
    
    private final VirtualSceneRepository virtualSceneRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * 根据ID获取虚拟场景详情
     * @param id 场景ID
     * @return 场景详情
     */
    public Optional<VirtualSceneDetailDto> getVirtualSceneDetail(Long id) {
        try {
            Optional<VirtualScene> scene = virtualSceneRepository.findById(id);
            if (scene.isPresent()) {
                VirtualScene virtualScene = scene.get();
                
                // 解析组件数据
                List<ComponentNode> components = null;
                if (virtualScene.getComponentsData() != null) {
                    ComponentNode[] componentArray = objectMapper.readValue(
                        virtualScene.getComponentsData(), ComponentNode[].class);
                    components = List.of(componentArray);
                }
                
                // 解析场景数据
                List<String> scenes = null;
                if (virtualScene.getScenesData() != null) {
                    String[] scenesArray = objectMapper.readValue(
                        virtualScene.getScenesData(), String[].class);
                    scenes = List.of(scenesArray);
                }
                
                VirtualSceneDetailDto dto = new VirtualSceneDetailDto();
                dto.setId(virtualScene.getId());
                dto.setName(virtualScene.getName());
                dto.setContent(virtualScene.getContent());
                dto.setComponents(components);
                dto.setScenes(scenes);
                
                return Optional.of(dto);
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("根据ID获取虚拟场景详情失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取虚拟场景详情失败", e);
        }
    }
    
    /**
     * 获取所有激活状态的虚拟场景
     * @return 场景列表
     */
    public List<VirtualScene> getAllActiveScenes() {
        try {
            return virtualSceneRepository.findByStatus("ACTIVE");
        } catch (Exception e) {
            log.error("获取虚拟场景列表失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取虚拟场景列表失败", e);
        }
    }
}