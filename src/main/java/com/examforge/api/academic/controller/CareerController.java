package com.examforge.api.academic.controller;

import com.examforge.api.academic.dto.CareerRequest;
import com.examforge.api.academic.dto.CareerResponse;
import com.examforge.api.academic.service.CareerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/careers")
@RequiredArgsConstructor
public class CareerController {
    private final CareerService careerService;

    @GetMapping
    public List<CareerResponse> getAllCareers() {
        return careerService.getAllCareers();
    }

    @GetMapping("/{id}")
    public CareerResponse getCareerById(@PathVariable Long id) {
        return careerService.getCareerById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public CareerResponse createCareer(@Valid @RequestBody CareerRequest request) {
        return careerService.createCareer(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CareerResponse updateCareer(@PathVariable Long id, @Valid @RequestBody CareerRequest request) {
        return careerService.updateCareer(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCareer(@PathVariable Long id) {
        careerService.deleteCareer(id);
    }
}
