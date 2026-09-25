package com.examforge.api.attempt.dto;

public record QuestionFeedbackResponse(
        Long questionId,
        String text,
        String topic,
        Long selectedOptionId,
        Long correctOptionId,
        boolean correct,
        String explanation
) {
}
