package com.examforge.api.assessment.generation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.examforge.api.ai.client.EmbeddingClient;
import com.examforge.api.ai.client.LlmClient;
import com.examforge.api.assessment.dto.AssessmentGenerateRequest;
import com.examforge.api.assessment.entity.Difficulty;
import com.examforge.api.assessment.entity.QuestionType;
import com.examforge.api.document.entity.DocumentChunk;
import com.examforge.api.document.repository.DocumentChunkRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AssessmentGeneratorTest {

    private static final String LLM_JSON = """
            {"questions":[
              {"text":"¿Qué exige la 1FN?","type":"MULTIPLE_CHOICE","difficulty":"EASY","topic":"1FN",
               "explanation":"Valores atómicos","sourceChunkIds":[11],
               "options":[{"text":"Valores atómicos","correct":true},{"text":"Claves foráneas","correct":false},
                          {"text":"Índices","correct":false},{"text":"Vistas","correct":false}]},
              {"text":"La 2FN elimina dependencias parciales","type":"TRUE_FALSE","topic":"2FN",
               "explanation":"Sí","sourceChunkIds":[11],
               "options":[{"text":"Verdadero","correct":true},{"text":"Falso","correct":false}]}
            ]}
            """;

    @Mock
    private EmbeddingClient embeddingClient;
    @Mock
    private LlmClient llmClient;
    @Mock
    private DocumentChunkRepository documentChunkRepository;
    @Mock
    private AssessmentGenerationWriter writer;

    private AssessmentGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new AssessmentGenerator(embeddingClient, llmClient, documentChunkRepository,
                new AssessmentPromptBuilder(), new GeneratedAssessmentParser(new ObjectMapper()), writer);
    }

    @Test
    @SuppressWarnings("unchecked")
    void savesOnlyQuestionsOfRequestedTypesWithTheirSources() {
        when(embeddingClient.embed(anyString())).thenReturn(new float[] {1f, 0f});
        when(documentChunkRepository.findMostSimilar(eq(List.of(7L)), eq("[1.0,0.0]"), eq(6)))
                .thenReturn(List.of(chunk(11L, new float[] {1f, 0f})));
        when(llmClient.generateJson(anyString(), anyString())).thenReturn(LLM_JSON);

        generator.generate(event(Set.of(QuestionType.MULTIPLE_CHOICE)));

        ArgumentCaptor<List<GeneratedQuestion>> questions = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Map<Long, Double>> relevance = ArgumentCaptor.forClass(Map.class);
        verify(writer).saveGenerated(eq(1L), questions.capture(), relevance.capture());
        assertThat(questions.getValue()).extracting(GeneratedQuestion::type).containsExactly(QuestionType.MULTIPLE_CHOICE);
        assertThat(questions.getValue().get(0).sourceChunkIds()).containsExactly(11L);
        assertThat(relevance.getValue().get(11L)).isEqualTo(1.0);
    }

    @Test
    void marksAssessmentAsFailedWhenDocumentsHaveNoContent() {
        when(embeddingClient.embed(anyString())).thenReturn(new float[] {1f, 0f});
        when(documentChunkRepository.findMostSimilar(anyList(), anyString(), eq(6))).thenReturn(List.of());

        generator.generate(event(Set.of(QuestionType.MULTIPLE_CHOICE)));

        verify(writer).markFailed(1L, "The selected documents have no indexed content");
        verify(writer, never()).saveGenerated(eq(1L), anyList(), anyMap());
    }

    private AssessmentGenerationRequestedEvent event(Set<QuestionType> types) {
        AssessmentGenerateRequest request = new AssessmentGenerateRequest(
                3L, "Normalización", List.of(7L), 2, Difficulty.EASY, types, null);
        return new AssessmentGenerationRequestedEvent(1L, List.of(7L), request);
    }

    private DocumentChunk chunk(Long id, float[] embedding) {
        DocumentChunk chunk = new DocumentChunk();
        ReflectionTestUtils.setField(chunk, "id", id);
        chunk.setContent("La primera forma normal exige valores atómicos.");
        chunk.setPageNumber(1);
        chunk.setEmbedding(embedding);
        return chunk;
    }
}
