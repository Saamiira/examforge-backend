package com.examforge.api.attempt.grading;

import java.math.BigDecimal;
import java.util.List;

public record GradingResult(
        int correctCount,
        int totalQuestions,
        BigDecimal score,
        List<TopicPerformance> performanceByTopic
) {
}
