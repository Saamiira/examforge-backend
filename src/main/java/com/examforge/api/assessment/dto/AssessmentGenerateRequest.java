package com.examforge.api.assessment.dto;

import java.util.List;
import java.util.Set;

import com.examforge.api.assessment.entity.Difficulty;
import com.examforge.api.assessment.entity.QuestionType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AssessmentGenerateRequest(
        @NotNull Long courseId,
        @NotBlank @Size(max = 150) String title,
        @NotEmpty List<Long> documentIds,
        @Min(1) @Max(20) int questionCount,
        @NotNull Difficulty difficulty,
        @NotEmpty Set<QuestionType> questionTypes,
        @Size(max = 200) String focusTopic
) {
}
