package com.examforge.api.assessment.generation;

import java.util.List;

import com.examforge.api.assessment.entity.Difficulty;
import com.examforge.api.assessment.entity.QuestionType;

public record GeneratedQuestion(
        String text,
        QuestionType type,
        Difficulty difficulty,
        String topic,
        String explanation,
        List<GeneratedOption> options,
        List<Long> sourceChunkIds
) {
}
