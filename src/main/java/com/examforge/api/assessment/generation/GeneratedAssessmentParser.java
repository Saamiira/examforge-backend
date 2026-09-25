package com.examforge.api.assessment.generation;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.examforge.api.assessment.entity.Difficulty;
import com.examforge.api.assessment.entity.QuestionType;
import com.examforge.api.common.exception.AiGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class GeneratedAssessmentParser {

    private static final int TRUE_FALSE_OPTIONS = 2;
    private static final int MIN_CHOICE_OPTIONS = 3;
    private static final int MAX_CHOICE_OPTIONS = 6;

    private final ObjectMapper mapper;

    public GeneratedAssessmentParser(ObjectMapper objectMapper) {
        this.mapper = objectMapper.copy()
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public List<GeneratedQuestion> parse(String json, Set<Long> allowedChunkIds, Difficulty fallbackDifficulty) {
        GeneratedAssessment generated = read(json);
        List<GeneratedQuestion> questions = generated.questions() == null ? List.of() : generated.questions();
        List<GeneratedQuestion> valid = questions.stream()
                .filter(Objects::nonNull)
                .map(question -> normalize(question, allowedChunkIds, fallbackDifficulty))
                .filter(this::isValid)
                .toList();
        if (valid.isEmpty()) {
            throw new AiGenerationException("Language model returned no valid questions");
        }
        return valid;
    }

    private GeneratedAssessment read(String json) {
        try {
            return mapper.readValue(json, GeneratedAssessment.class);
        } catch (JsonProcessingException ex) {
            throw new AiGenerationException("Language model returned invalid JSON");
        }
    }

    private GeneratedQuestion normalize(GeneratedQuestion question, Set<Long> allowedChunkIds,
                                       Difficulty fallbackDifficulty) {
        List<Long> sources = question.sourceChunkIds() == null ? List.of()
                : question.sourceChunkIds().stream().filter(allowedChunkIds::contains).distinct().toList();
        Difficulty difficulty = question.difficulty() == null ? fallbackDifficulty : question.difficulty();
        return new GeneratedQuestion(question.text(), question.type(), difficulty, question.topic(),
                question.explanation(), question.options(), sources);
    }

    private boolean isValid(GeneratedQuestion question) {
        if (isBlank(question.text()) || question.type() == null || question.options() == null
                || question.sourceChunkIds().isEmpty()) {
            return false;
        }
        boolean optionsHaveText = question.options().stream()
                .allMatch(option -> option != null && !isBlank(option.text()));
        long correctCount = question.options().stream().filter(GeneratedOption::correct).count();
        return optionsHaveText && correctCount == 1 && hasValidOptionCount(question);
    }

    private boolean hasValidOptionCount(GeneratedQuestion question) {
        int size = question.options().size();
        if (question.type() == QuestionType.TRUE_FALSE) {
            return size == TRUE_FALSE_OPTIONS;
        }
        return size >= MIN_CHOICE_OPTIONS && size <= MAX_CHOICE_OPTIONS;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
