package com.examforge.api.attempt.dto;

import java.time.Instant;
import java.util.List;

import com.examforge.api.attempt.entity.AttemptStatus;

public record AttemptStartResponse(
        Long attemptId,
        Long assessmentId,
        String assessmentTitle,
        AttemptStatus status,
        Instant startedAt,
        List<AttemptQuestionResponse> questions
) {
}
