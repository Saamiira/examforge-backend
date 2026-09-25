package com.examforge.api.attempt.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.examforge.api.attempt.entity.AttemptStatus;

public record AttemptSummaryResponse(
        Long attemptId,
        Long assessmentId,
        String assessmentTitle,
        AttemptStatus status,
        BigDecimal score,
        Instant submittedAt
) {
}
