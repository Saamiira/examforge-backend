package com.examforge.api.attempt.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AttemptSubmitRequest(
        @NotNull @Size(max = 100) List<@Valid AnswerRequest> answers
) {
}
