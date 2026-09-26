package com.examforge.api.attempt.dto;

import jakarta.validation.constraints.NotNull;

public record AnswerRequest(
        @NotNull Long questionId,
        Long selectedOptionId
) {
}
