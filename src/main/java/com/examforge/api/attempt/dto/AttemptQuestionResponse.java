package com.examforge.api.attempt.dto;

import java.util.List;

import com.examforge.api.assessment.entity.QuestionType;

public record AttemptQuestionResponse(
        Long id,
        String text,
        QuestionType type,
        int position,
        List<AttemptOptionResponse> options
) {
}
