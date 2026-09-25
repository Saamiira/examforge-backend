package com.examforge.api.assessment.dto;

import com.examforge.api.assessment.entity.Difficulty;
import com.examforge.api.assessment.entity.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AssessmentUpdateRequest(
        @NotBlank @Size(max = 150) String title,
        @Size(max = 1000) String description,
        @NotNull Difficulty difficulty,
        @NotNull Visibility visibility
) {
}
