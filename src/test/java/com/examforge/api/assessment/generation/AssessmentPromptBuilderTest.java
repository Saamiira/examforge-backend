package com.examforge.api.assessment.generation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;

import com.examforge.api.assessment.dto.AssessmentGenerateRequest;
import com.examforge.api.assessment.entity.Difficulty;
import com.examforge.api.assessment.entity.QuestionType;
import org.junit.jupiter.api.Test;

class AssessmentPromptBuilderTest {

    private final AssessmentPromptBuilder builder = new AssessmentPromptBuilder();

    @Test
    void userPromptIncludesParametersAndChunkReferences() {
        AssessmentGenerateRequest request = new AssessmentGenerateRequest(
                1L, "Parcial", List.of(10L), 5, Difficulty.MEDIUM,
                Set.of(QuestionType.MULTIPLE_CHOICE), null);
        List<ContextChunk> chunks = List.of(new ContextChunk(42L, 3, "La mitocondria produce ATP."));

        String prompt = builder.buildUserPrompt(request, chunks);

        assertThat(prompt)
                .contains("Genera 5 preguntas")
                .contains("MEDIUM")
                .contains("id=42")
                .contains("página 3")
                .contains("La mitocondria produce ATP.")
                .contains("todo el contenido");
    }

    @Test
    void systemPromptRequiresJsonAndSourceTraceability() {
        assertThat(builder.systemPrompt())
                .contains("JSON")
                .contains("sourceChunkIds")
                .contains("EXCLUSIVAMENTE");
    }
}
