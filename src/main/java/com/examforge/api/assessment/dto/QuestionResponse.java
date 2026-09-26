package com.examforge.api.assessment.dto;

import java.util.List;

import com.examforge.api.assessment.entity.Difficulty;
import com.examforge.api.assessment.entity.QuestionType;

public record QuestionResponse(
        Long id,
        String text,
        QuestionType type,
        Difficulty difficulty,
        String topic,
        String explanation,
        int position,
        List<OptionResponse> options
) {
}
