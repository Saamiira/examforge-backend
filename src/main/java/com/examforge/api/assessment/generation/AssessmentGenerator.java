package com.examforge.api.assessment.generation;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.examforge.api.ai.client.EmbeddingClient;
import com.examforge.api.ai.client.LlmClient;
import com.examforge.api.assessment.dto.AssessmentGenerateRequest;
import com.examforge.api.assessment.entity.QuestionType;
import com.examforge.api.common.exception.AiGenerationException;
import com.examforge.api.document.entity.DocumentChunk;
import com.examforge.api.document.repository.DocumentChunkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * RAG pipeline: embed the request, retrieve the closest fragments with pgvector,
 * ask the language model for questions grounded in them and persist the validated result.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AssessmentGenerator {

    private static final int CHUNKS_PER_QUESTION = 3;
    private static final int MAX_CONTEXT_CHUNKS = 30;

    private final EmbeddingClient embeddingClient;
    private final LlmClient llmClient;
    private final DocumentChunkRepository documentChunkRepository;
    private final AssessmentPromptBuilder promptBuilder;
    private final GeneratedAssessmentParser parser;
    private final AssessmentGenerationWriter writer;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGenerationRequested(AssessmentGenerationRequestedEvent event) {
        generate(event);
    }

    public void generate(AssessmentGenerationRequestedEvent event) {
        Long assessmentId = event.assessmentId();
        try {
            AssessmentGenerateRequest request = event.request();
            float[] queryVector = embeddingClient.embed(buildQuery(request));

            int limit = Math.min(request.questionCount() * CHUNKS_PER_QUESTION, MAX_CONTEXT_CHUNKS);
            List<DocumentChunk> chunks = documentChunkRepository.findMostSimilar(
                    event.documentIds(), VectorUtils.toPgVector(queryVector), limit);
            if (chunks.isEmpty()) {
                throw new AiGenerationException("The selected documents have no indexed content");
            }

            List<ContextChunk> context = chunks.stream()
                    .map(chunk -> new ContextChunk(chunk.getId(), chunk.getPageNumber(), chunk.getContent()))
                    .toList();
            String json = llmClient.generateJson(
                    promptBuilder.systemPrompt(), promptBuilder.buildUserPrompt(request, context));

            Map<Long, DocumentChunk> chunksById = chunks.stream()
                    .collect(Collectors.toMap(DocumentChunk::getId, Function.identity()));
            List<GeneratedQuestion> questions = selectQuestions(
                    parser.parse(json, chunksById.keySet(), request.difficulty()), request);

            Map<Long, Double> relevanceByChunk = chunks.stream()
                    .collect(Collectors.toMap(DocumentChunk::getId,
                            chunk -> VectorUtils.cosineSimilarity(queryVector, chunk.getEmbedding())));

            writer.saveGenerated(assessmentId, questions, relevanceByChunk);
            log.info("Assessment {} generated with {} questions", assessmentId, questions.size());
        } catch (Exception ex) {
            log.error("Generation failed for assessment {}", assessmentId, ex);
            String reason = ex instanceof AiGenerationException ? ex.getMessage() : "Unexpected error while generating questions";
            writer.markFailed(assessmentId, reason);
        }
    }

    private List<GeneratedQuestion> selectQuestions(List<GeneratedQuestion> parsed, AssessmentGenerateRequest request) {
        Set<QuestionType> allowedTypes = request.questionTypes();
        List<GeneratedQuestion> selected = parsed.stream()
                .filter(question -> allowedTypes.contains(question.type()))
                .limit(request.questionCount())
                .toList();
        if (selected.isEmpty()) {
            throw new AiGenerationException("The language model returned no questions of the requested types");
        }
        return selected;
    }

    private String buildQuery(AssessmentGenerateRequest request) {
        String focus = request.focusTopic() == null || request.focusTopic().isBlank() ? "" : " " + request.focusTopic();
        return request.title() + focus;
    }
}
