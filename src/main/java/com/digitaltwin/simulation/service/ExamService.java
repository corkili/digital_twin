package com.digitaltwin.simulation.service;

import com.digitaltwin.simulation.dto.CreateExamRequest;
import com.digitaltwin.simulation.dto.ExamDto;
import com.digitaltwin.simulation.dto.UpdateExamRequest;
import com.digitaltwin.simulation.entity.ExamRecord;
import com.digitaltwin.simulation.repository.ExamRepository;
import com.digitaltwin.system.entity.User;
import com.digitaltwin.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamRepository examRepository;
    private final UserRepository userRepository;

    public ExamDto createExam(CreateExamRequest request) {
        try {
            ExamRecord e = new ExamRecord();
            e.setUserId(request.getUserId());
            e.setMode(request.getMode());
            e.setExperimentName(request.getExperimentName());
            e.setExperimentTime(request.getExperimentTime());
            e.setScore(request.getScore());
            
            ExamRecord savedExam = examRepository.save(e);
            return createExamDtoWithUserName(savedExam);
        } catch (Exception ex) {
            log.error("创建Exam失败: {}", ex.getMessage(), ex);
            throw new RuntimeException("创建Exam失败", ex);
        }
    }
    
    /**
     * 根据userId创建考试记录（用于仿真系统自动创建）
     */
    public ExamDto createExamByUserId(Long userId, String mode, String experimentName, 
                                     LocalDateTime experimentTime, Integer score) {
        try {
            ExamRecord e = new ExamRecord();
            e.setUserId(userId);
            e.setMode(mode);
            e.setExperimentName(experimentName);
            e.setExperimentTime(experimentTime);
            e.setScore(score);
            
            ExamRecord savedExam = examRepository.save(e);
            log.info("自动创建考试记录: userId={}, experimentName={}, score={}", 
                    userId, experimentName, score);
            return createExamDtoWithUserName(savedExam);
        } catch (Exception ex) {
            log.error("自动创建Exam失败: {}", ex.getMessage(), ex);
            throw new RuntimeException("自动创建Exam失败", ex);
        }
    }

    public Optional<ExamDto> getById(Long id) {
        return examRepository.findById(id).map(this::createExamDtoWithUserName);
    }

    public List<ExamDto> getAll() {
        return examRepository.findAll().stream()
                .map(this::createExamDtoWithUserName)
                .collect(Collectors.toList());
    }

    public Page<ExamDto> getPage(int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return examRepository.findAll(pageable).map(this::createExamDtoWithUserName);
    }

    public List<ExamDto> searchByUserName(String userName) {
        // 通过用户姓名搜索，需要联表查询
        List<User> users = userRepository.findByFullNameContainingIgnoreCase(userName);
        List<Long> userIds = users.stream()
                .map(User::getId)
                .filter(userId -> userId != null)
                .collect(Collectors.toList());
        
        if (userIds.isEmpty()) {
            return List.of();
        }
        
        return examRepository.findByUserIdIn(userIds)
                .stream().map(this::createExamDtoWithUserName)
                .collect(Collectors.toList());
    }

    public Page<ExamDto> searchByUserNameWithPagination(String userName, int page, int size, String sortBy, String sortDir) {
        // 通过用户姓名搜索，需要联表查询
        List<User> users = userRepository.findByFullNameContainingIgnoreCase(userName);
        List<Long> userIds = users.stream()
                .map(User::getId)
                .filter(userId -> userId != null)
                .collect(Collectors.toList());
        
        if (userIds.isEmpty()) {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            return examRepository.findByUserIdIn(List.of(-1L), pageable)
                    .map(this::createExamDtoWithUserName);
        }
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return examRepository.findByUserIdIn(userIds, pageable)
                .map(this::createExamDtoWithUserName);
    }

    public Optional<ExamDto> update(Long id, UpdateExamRequest request) {
        return examRepository.findById(id).map(e -> {
            if (request.getUserId() != null) e.setUserId(request.getUserId());
            if (request.getMode() != null) e.setMode(request.getMode());
            if (request.getExperimentName() != null) e.setExperimentName(request.getExperimentName());
            if (request.getExperimentTime() != null) e.setExperimentTime(request.getExperimentTime());
            if (request.getScore() != null) e.setScore(request.getScore());
            return createExamDtoWithUserName(examRepository.save(e));
        });
    }

    public boolean delete(Long id) {
        if (!examRepository.existsById(id)) return false;
        examRepository.deleteById(id);
        return true;
    }
    
    /**
     * 创建包含用户姓名的ExamDto
     */
    private ExamDto createExamDtoWithUserName(ExamRecord examRecord) {
        if (examRecord == null) return null;
        
        String userName = "未知用户";
        if (examRecord.getUserId() != null) {
            Optional<User> userOpt = userRepository.findById(examRecord.getUserId());
            if (userOpt.isPresent() && userOpt.get().getFullName() != null) {
                userName = userOpt.get().getFullName();
            }
        }
        
        return ExamDto.fromEntityWithUserName(examRecord, userName);
    }
}
