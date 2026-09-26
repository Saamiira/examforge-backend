package com.examforge.api.assessment.generation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;

import com.examforge.api.assessment.entity.Difficulty;
import com.examforge.api.assessment.entity.QuestionType;
import com.examforge.api.common.exception.AiGenerationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class GeneratedAssessmentParserTest {

    private static final Set<Long> ALLOWED_CHUNKS = Set.of(1L, 2L);

    private final GeneratedAssessmentParser parser = new GeneratedAssessmentParser(new ObjectMapper());

    @Test
    void parsesValidQuestionWithCaseInsensitiveEnums() {
        String json = """
                {"questions":[{"text":"¿Qué produce la mitocondria?","type":"multiple_choice","difficulty":"easy",
                "topic":"Célula","explanation":"Produce ATP.","sourceChunkIds":[1],
                "options":[{"text":"ATP","correct":true},{"text":"ADN","correct":false},
                {"text":"Glucosa","correct":false},{"text":"Oxígeno","correct":false}]}]}
                """;

        List<GeneratedQuestion> questions = parser.parse(json, ALLOWED_CHUNKS, Difficulty.MEDIUM);

        assertThat(questions).hasSize(1);
        assertThat(questions.get(0).type()).isEqualTo(QuestionType.MULTIPLE_CHOICE);
        assertThat(questions.get(0).difficulty()).isEqualTo(Difficulty.EASY);
    }

    @Test
    void discardsQuestionWithTwoCorrectOptionsAndKeepsValidOne() {
        String json = """
                {"questions":[
                {"text":"Inválida","type":"TRUE_FALSE","sourceChunkIds":[1],
                 "options":[{"text":"Verdadero","correct":true},{"text":"Falso","correct":true}]},
                {"text":"Válida","type":"TRUE_FALSE","sourceChunkIds":[2],
                 "options":[{"text":"Verdadero","correct":true},{"text":"Falso","correct":false}]}]}
                """;

        List<GeneratedQuestion> questions = parser.parse(json, ALLOWED_CHUNKS, Difficulty.MEDIUM);

        assertThat(questions).extracting(GeneratedQuestion::text).containsExactly("Válida");
        assertThat(questions.get(0).difficulty()).isEqualTo(Difficulty.MEDIUM);
    }

    @Test
    void removesUnknownSourceChunks() {
        String json = """
                {"questions":[{"text":"Pregunta","type":"TRUE_FALSE","sourceChunkIds":[2, 99],
                 "options":[{"text":"Verdadero","correct":false},{"text":"Falso","correct":true}]}]}
                """;

        List<GeneratedQuestion> questions = parser.parse(json, ALLOWED_CHUNKS, Difficulty.HARD);

        assertThat(questions.get(0).sourceChunkIds()).containsExactly(2L);
    }

    @Test
    void throwsWhenJsonIsMalformed() {
        assertThatThrownBy(() -> parser.parse("esto no es json", ALLOWED_CHUNKS, Difficulty.EASY))
                .isInstanceOf(AiGenerationException.class)
                .hasMessageContaining("invalid JSON");
    }

    @Test
    void throwsWhenNoQuestionIsValid() {
        String json = """
                {"questions":[{"text":"Sin fuentes","type":"TRUE_FALSE","sourceChunkIds":[99],
                 "options":[{"text":"Verdadero","correct":true},{"text":"Falso","correct":false}]}]}
                """;

        assertThatThrownBy(() -> parser.parse(json, ALLOWED_CHUNKS, Difficulty.EASY))
                .isInstanceOf(AiGenerationException.class)
                .hasMessageContaining("no valid questions");
    }
}
