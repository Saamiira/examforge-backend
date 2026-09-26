package com.examforge.api.academic.service;

import com.examforge.api.academic.dto.UniversityRequest;
import com.examforge.api.academic.dto.UniversityResponse;
import com.examforge.api.academic.entity.University;
import com.examforge.api.academic.mapper.UniversityMapper;
import com.examforge.api.academic.repository.UniversityRepository;
import com.examforge.api.common.exception.DuplicateResourceException;
import com.examforge.api.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UniversityService {
    private final UniversityRepository universityRepository;
    private final UniversityMapper universityMapper;

    @Transactional(readOnly = true)
    public List<UniversityResponse> getAllUniversities() {
        return universityRepository.findAll().stream()
                .map(universityMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UniversityResponse getUniversityById(Long id) {
        University university = universityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("University not found"));
        return universityMapper.toResponse(university);
    }

    @Transactional
    public UniversityResponse createUniversity(UniversityRequest request) {
        if (universityRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("University name already exists");
        }
        University university = universityMapper.toEntity(request);
        return universityMapper.toResponse(universityRepository.save(university));
    }

    @Transactional
    public UniversityResponse updateUniversity(Long id, UniversityRequest request) {
        University university = universityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("University not found"));
        
        if (!university.getName().equals(request.getName()) && universityRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("University name already exists");
        }
        
        universityMapper.updateEntityFromRequest(request, university);
        return universityMapper.toResponse(universityRepository.save(university));
    }

    @Transactional
    public void deleteUniversity(Long id) {
        if (!universityRepository.existsById(id)) {
            throw new ResourceNotFoundException("University not found");
        }
        universityRepository.deleteById(id);
    }
}
