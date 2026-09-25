package com.examforge.api.academic.service;

import com.examforge.api.academic.dto.CareerRequest;
import com.examforge.api.academic.dto.CareerResponse;
import com.examforge.api.academic.entity.Career;
import com.examforge.api.academic.entity.University;
import com.examforge.api.academic.mapper.CareerMapper;
import com.examforge.api.academic.repository.CareerRepository;
import com.examforge.api.academic.repository.UniversityRepository;
import com.examforge.api.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CareerService {
    private final CareerRepository careerRepository;
    private final UniversityRepository universityRepository;
    private final CareerMapper careerMapper;

    @Transactional(readOnly = true)
    public List<CareerResponse> getAllCareers() {
        return careerRepository.findAll().stream()
                .map(careerMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CareerResponse getCareerById(Long id) {
        Career career = careerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Career not found"));
        return careerMapper.toResponse(career);
    }

    @Transactional
    public CareerResponse createCareer(CareerRequest request) {
        University university = universityRepository.findById(request.getUniversityId())
                .orElseThrow(() -> new ResourceNotFoundException("University not found"));
                
        Career career = careerMapper.toEntity(request);
        career.setUniversity(university);
        return careerMapper.toResponse(careerRepository.save(career));
    }

    @Transactional
    public CareerResponse updateCareer(Long id, CareerRequest request) {
        Career career = careerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Career not found"));
                
        if (!career.getUniversity().getId().equals(request.getUniversityId())) {
            University university = universityRepository.findById(request.getUniversityId())
                    .orElseThrow(() -> new ResourceNotFoundException("University not found"));
            career.setUniversity(university);
        }
        
        careerMapper.updateEntityFromRequest(request, career);
        return careerMapper.toResponse(careerRepository.save(career));
    }

    @Transactional
    public void deleteCareer(Long id) {
        if (!careerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Career not found");
        }
        careerRepository.deleteById(id);
    }
}
