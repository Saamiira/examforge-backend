package com.examforge.api.assessment.dto;

import java.time.Instant;

import com.examforge.api.assessment.entity.AssessmentStatus;
import com.examforge.api.assessment.entity.Difficulty;
import com.examforge.api.assessment.entity.Visibility;

public record AssessmentSummaryResponse(
        Long id,
        String title,
        Difficulty difficulty,
        AssessmentStatus status,
        Visibility visibility,
        int questionCount,
        Long courseId,
        Long authorId,
        Instant createdAt
) {
}
