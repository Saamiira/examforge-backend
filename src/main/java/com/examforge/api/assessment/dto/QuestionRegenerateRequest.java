package com.examforge.api.assessment.dto;

import com.examforge.api.assessment.entity.Difficulty;
import jakarta.validation.constraints.Size;

public record QuestionRegenerateRequest(
        Difficulty difficulty,
        @Size(max = 300) String instruction
) {
}
