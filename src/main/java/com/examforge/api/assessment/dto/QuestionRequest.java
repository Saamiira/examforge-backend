package com.examforge.api.assessment.dto;

import java.util.List;

import com.examforge.api.assessment.entity.Difficulty;
import com.examforge.api.assessment.entity.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record QuestionRequest(
        @NotBlank @Size(max = 2000) String text,
        @NotNull QuestionType type,
        @NotNull Difficulty difficulty,
        @Size(max = 120) String topic,
        @Size(max = 2000) String explanation,
        @NotNull @Size(min = 2, max = 6) List<@Valid OptionRequest> options
) {
}
