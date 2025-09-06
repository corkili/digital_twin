package com.digitaltwin.simulation.service;

import com.digitaltwin.simulation.dto.SimulationExperimentDto;
import com.digitaltwin.simulation.dto.SimulationExperimentListDto;
import com.digitaltwin.simulation.dto.SimulationStepNode;
import com.digitaltwin.simulation.dto.ExperimentStepDto;
import com.digitaltwin.simulation.dto.ExperimentDescriptionDto;
import com.digitaltwin.simulation.entity.SimulationExperiment;
import com.digitaltwin.simulation.repository.SimulationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SimulationService {
    
    private final SimulationRepository simulationRepository;
    private final ObjectMapper objectMapper;
    
    /**
     * 获取所有模拟试验列表
     * @return 试验列表
     */
    public List<SimulationExperimentListDto> getAllExperiments() {
        try {
            List<SimulationExperiment> experiments = simulationRepository.findAll();
            return experiments.stream()
                    .map(SimulationExperimentListDto::fromEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取试验列表失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取试验列表失败", e);
        }
    }
    
    /**
     * 根据ID获取试验详情
     * @param id 试验ID
     * @return 试验详情
     */
    public Optional<SimulationExperimentDto> getExperimentById(Long id) {
        try {
            Optional<SimulationExperiment> experiment = simulationRepository.findById(id);
            return experiment.map(SimulationExperimentDto::fromEntity);
        } catch (Exception e) {
            log.error("根据ID获取试验详情失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取试验详情失败", e);
        }
    }
    
    /**
     * 根据ID获取试验介绍
     * @param id 试验ID
     * @return 试验名称和介绍
     */
    public Optional<ExperimentDescriptionDto> getExperimentDescription(Long id) {
        try {
            Optional<SimulationExperiment> experiment = simulationRepository.findById(id);
            return experiment.map(exp -> new ExperimentDescriptionDto(exp.getName(), exp.getDescription()));
        } catch (Exception e) {
            log.error("根据ID获取试验介绍失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取试验介绍失败", e);
        }
    }
    
    /**
     * 根据状态获取试验列表
     * @param status 试验状态
     * @return 试验列表
     */
    public List<SimulationExperimentListDto> getExperimentsByStatus(String status) {
        try {
            List<SimulationExperiment> experiments = simulationRepository.findByStatus(status);
            return experiments.stream()
                    .map(SimulationExperimentListDto::fromEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("根据状态获取试验列表失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取试验列表失败", e);
        }
    }
    
    
    /**
     * 根据ID获取试验步骤
     * @param id 试验ID
     * @return 试验步骤数据
     */
    public Optional<ExperimentStepDto> getExperimentSteps(Long id) {
        try {
            Optional<SimulationExperiment> experiment = simulationRepository.findById(id);
            if (experiment.isPresent() && experiment.get().getStepsData() != null) {
                String stepsData = experiment.get().getStepsData();
                ExperimentStepDto steps = objectMapper.readValue(stepsData, ExperimentStepDto.class);
                return Optional.of(steps);
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("根据ID获取试验步骤失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取试验步骤失败", e);
        }
    }
}