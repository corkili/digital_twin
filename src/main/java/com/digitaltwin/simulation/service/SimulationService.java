package com.digitaltwin.simulation.service;

import com.digitaltwin.simulation.dto.SimulationExperimentDto;
import com.digitaltwin.simulation.dto.SimulationExperimentListDto;
import com.digitaltwin.simulation.dto.SimulationStepNode;
import com.digitaltwin.simulation.dto.ExperimentStepDto;
import com.digitaltwin.simulation.dto.ExperimentStepsDto;
import com.digitaltwin.simulation.dto.ExperimentDescriptionDto;
import com.digitaltwin.simulation.dto.SubmitExperimentStepRequest;
import com.digitaltwin.simulation.dto.RoleDto;
import com.digitaltwin.simulation.entity.SimulationExperiment;
import com.digitaltwin.simulation.entity.UserExperimentRecord;
import com.digitaltwin.simulation.repository.SimulationRepository;
import com.digitaltwin.simulation.repository.UserExperimentRecordRepository;
import com.digitaltwin.simulation.service.ExamService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SimulationService {
    
    private final SimulationRepository simulationRepository;
    private final UserExperimentRecordRepository userExperimentRecordRepository;
    private final ExamService examService;
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
     * @return 试验步骤数据（包含步骤数组）
     */
    public Optional<ExperimentStepsDto> getExperimentSteps(Long id) {
        try {
            Optional<SimulationExperiment> experiment = simulationRepository.findById(id);
            if (experiment.isPresent() && experiment.get().getStepsData() != null) {
                String stepsData = experiment.get().getStepsData();
                
                // 尝试解析为步骤数组
                List<ExperimentStepDto> stepsList = objectMapper.readValue(
                    stepsData, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ExperimentStepDto.class)
                );
                
                ExperimentStepsDto result = new ExperimentStepsDto();
                result.setSteps(stepsList);
                result.setTotalSteps(stepsList.size());
                result.setExperimentName(experiment.get().getName());
                
                return Optional.of(result);
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("根据ID获取试验步骤失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取试验步骤失败", e);
        }
    }
    
    /**
     * 提交试验步骤
     * @param request 提交请求
     * @return 提交结果ID
     */
    @Transactional
    public Long submitExperimentStep(SubmitExperimentStepRequest request) {
        try {
            // 验证试验是否存在
            if (!simulationRepository.existsById(request.getTargetExperimentId())) {
                throw new RuntimeException("试验ID不存在: " + request.getTargetExperimentId());
            }
            
            // 将ExperimentStepDto列表序列化为JSON
            String stepDataJson = objectMapper.writeValueAsString(request.getExperimentSteps());
            
            // 计算得分
            Integer score = calculateScore(request.getTargetExperimentId(), request.getExperimentSteps());
            
            // 创建新记录（不查重，全部新增）
            UserExperimentRecord record = new UserExperimentRecord();
            record.setUserId(request.getUserId());
            record.setTargetExperimentId(request.getTargetExperimentId());
            record.setStepData(stepDataJson);
            record.setScore(score);
            
            UserExperimentRecord savedRecord = userExperimentRecordRepository.save(record);
            log.info("创建新的用户试验记录: recordId={}, userId={}, experimentId={}, score={}", 
                savedRecord.getId(), request.getUserId(), request.getTargetExperimentId(), score);
            
            // 自动创建Exam考试记录
            try {
                createExamRecord(request, score);
            } catch (Exception examEx) {
                log.error("创建Exam记录失败，但用户试验记录已保存: {}", examEx.getMessage(), examEx);
            }
            
            return savedRecord.getId();
        } catch (Exception e) {
            log.error("提交试验步骤失败: {}", e.getMessage(), e);
            throw new RuntimeException("提交试验步骤失败", e);
        }
    }
    
    /**
     * 计算用户提交步骤的得分
     * 
     * @param targetExperimentId 目标试验ID（标准答案）
     * @param userSubmission 用户提交的试验步骤数组
     * @return 得分（0-100的整数）
     */
    private Integer calculateScore(Long targetExperimentId, List<ExperimentStepDto> userSubmission) {
        try {
            // 获取标准答案
            Optional<ExperimentStepsDto> standardAnswerOpt = getExperimentSteps(targetExperimentId);
            if (!standardAnswerOpt.isPresent()) {
                log.warn("未找到试验ID {} 的标准答案", targetExperimentId);
                return 0;
            }
            
            ExperimentStepsDto standardAnswer = standardAnswerOpt.get();
            
            // 提取标准答案中的所有SimulationStepNode名称（按顺序）
            List<String> standardNames = extractSimulationStepNodeNamesFromSteps(standardAnswer.getSteps());
            
            // 提取用户提交中的所有SimulationStepNode名称（按顺序）
            List<String> userNames = extractSimulationStepNodeNamesFromSteps(userSubmission);
            
            // 计算得分
            if (standardNames.isEmpty()) {
                return 0;
            }
            
            int totalNodes = standardNames.size();
            int scorePerNode = 100 / totalNodes; // 向下取整
            int correctCount = 0;
            
            // 按顺序比对名称
            int minLength = Math.min(standardNames.size(), userNames.size());
            for (int i = 0; i < minLength; i++) {
                if (standardNames.get(i).equals(userNames.get(i))) {
                    correctCount++;
                }
            }
            
            int finalScore = correctCount * scorePerNode;
            
            log.debug("打分详情 - 总节点数: {}, 每节点分值: {}, 正确数量: {}, 最终得分: {}", 
                totalNodes, scorePerNode, correctCount, finalScore);
            
            return finalScore;
            
        } catch (Exception e) {
            log.error("计算得分失败: {}", e.getMessage(), e);
            return 0;
        }
    }
    
    /**
     * 提取步骤数组中所有SimulationStepNode的name字段
     * 
     * @param experimentSteps 试验步骤数组
     * @return 按顺序排列的name列表
     */
    private List<String> extractSimulationStepNodeNamesFromSteps(List<ExperimentStepDto> experimentSteps) {
        List<String> names = new ArrayList<>();
        
        if (experimentSteps == null || experimentSteps.isEmpty()) {
            return names;
        }
        
        // 遍历所有步骤
        for (ExperimentStepDto step : experimentSteps) {
            List<String> stepNames = extractSimulationStepNodeNames(step);
            names.addAll(stepNames);
        }
        
        return names;
    }
    
    /**
     * 递归提取ExperimentStepDto中所有SimulationStepNode的name字段
     * 
     * @param experimentStep 试验步骤DTO
     * @return 按顺序排列的name列表
     */
    private List<String> extractSimulationStepNodeNames(ExperimentStepDto experimentStep) {
        List<String> names = new ArrayList<>();
        
        if (experimentStep == null || experimentStep.getRoles() == null) {
            return names;
        }
        
        // 遍历所有角色
        for (RoleDto role : experimentStep.getRoles()) {
            if (role.getTasks() != null) {
                // 遍历每个角色的任务节点
                for (SimulationStepNode task : role.getTasks()) {
                    extractNodeNames(task, names);
                }
            }
        }
        
        return names;
    }
    
    /**
     * 递归提取SimulationStepNode中的name（只提取第一层，不包括嵌套的child节点）
     * 
     * @param node 步骤节点
     * @param names 名称列表
     */
    private void extractNodeNames(SimulationStepNode node, List<String> names) {
        if (node != null && node.getName() != null) {
            names.add(node.getName());
        }
        // 注意：根据需求，这里不递归处理child节点，只处理第一层
    }
    
    /**
     * 自动创建Exam考试记录
     * 
     * @param request 提交请求
     * @param score 计算出的分数
     */
    private void createExamRecord(SubmitExperimentStepRequest request, Integer score) {
        try {
            // 获取试验名称
            String experimentName = "未知试验";
            Optional<SimulationExperiment> experimentOpt = simulationRepository.findById(request.getTargetExperimentId());
            if (experimentOpt.isPresent()) {
                experimentName = experimentOpt.get().getName();
            }
            
            // 调用ExamService创建考试记录
            examService.createExamByUserId(
                request.getUserId(),
                "自动", // 模式设为自动
                experimentName,
                LocalDateTime.now(), // 考试时间为当前时间
                score
            );
            
            log.info("自动创建Exam记录成功: userId={}, experimentName={}, score={}", 
                    request.getUserId(), experimentName, score);
            
        } catch (Exception e) {
            log.error("自动创建Exam记录失败: userId={}, error={}", 
                    request.getUserId(), e.getMessage(), e);
            throw e;
        }
    }
}