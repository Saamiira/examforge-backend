package com.examforge.api.attempt.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.examforge.api.attempt.entity.AttemptStatus;
import com.examforge.api.attempt.grading.TopicPerformance;

public record AttemptResultResponse(
        Long attemptId,
        Long assessmentId,
        AttemptStatus status,
        Instant startedAt,
        Instant submittedAt,
        int correctCount,
        int totalQuestions,
        BigDecimal score,
        List<TopicPerformance> performanceByTopic,
        List<QuestionFeedbackResponse> feedback
) {
}
