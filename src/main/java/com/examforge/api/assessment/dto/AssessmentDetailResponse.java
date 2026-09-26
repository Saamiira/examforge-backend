package com.examforge.api.assessment.dto;

import java.time.Instant;
import java.util.List;

import com.examforge.api.assessment.entity.AssessmentStatus;
import com.examforge.api.assessment.entity.Difficulty;
import com.examforge.api.assessment.entity.Visibility;

public record AssessmentDetailResponse(
        Long id,
        String title,
        String description,
        Difficulty difficulty,
        AssessmentStatus status,
        String failureReason,
        Visibility visibility,
        Long courseId,
        Long authorId,
        Instant createdAt,
        List<QuestionResponse> questions
) {
}
