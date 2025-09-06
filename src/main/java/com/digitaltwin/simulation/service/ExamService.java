package com.digitaltwin.simulation.service;

import com.digitaltwin.simulation.dto.CreateExamRequest;
import com.digitaltwin.simulation.dto.ExamDto;
import com.digitaltwin.simulation.dto.UpdateExamRequest;
import com.digitaltwin.simulation.entity.ExamRecord;
import com.digitaltwin.simulation.repository.ExamRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamRepository examRepository;

    public ExamDto createExam(CreateExamRequest request) {
        try {
            ExamRecord e = new ExamRecord();
            e.setName(request.getName());
            e.setMode(request.getMode());
            e.setExperimentName(request.getExperimentName());
            e.setExperimentTime(request.getExperimentTime());
            return ExamDto.fromEntity(examRepository.save(e));
        } catch (Exception ex) {
            log.error("创建Exam失败: {}", ex.getMessage(), ex);
            throw new RuntimeException("创建Exam失败", ex);
        }
    }

    public Optional<ExamDto> getById(Long id) {
        return examRepository.findById(id).map(ExamDto::fromEntity);
    }

    public List<ExamDto> getAll() {
        return examRepository.findAll().stream().map(ExamDto::fromEntity).collect(Collectors.toList());
    }

    public Page<ExamDto> getPage(int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return examRepository.findAll(pageable).map(ExamDto::fromEntity);
    }

    public List<ExamDto> searchByName(String name) {
        return examRepository.findByNameContainingIgnoreCase(name)
                .stream().map(ExamDto::fromEntity).collect(Collectors.toList());
    }

    public Page<ExamDto> searchByNameWithPagination(String name, int page, int size, String sortBy, String sortDir) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        return examRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(ExamDto::fromEntity);
    }

    public Optional<ExamDto> update(Long id, UpdateExamRequest request) {
        return examRepository.findById(id).map(e -> {
            if (request.getName() != null) e.setName(request.getName());
            if (request.getMode() != null) e.setMode(request.getMode());
            if (request.getExperimentName() != null) e.setExperimentName(request.getExperimentName());
            if (request.getExperimentTime() != null) e.setExperimentTime(request.getExperimentTime());
            return ExamDto.fromEntity(examRepository.save(e));
        });
    }

    public boolean delete(Long id) {
        if (!examRepository.existsById(id)) return false;
        examRepository.deleteById(id);
        return true;
    }
}
