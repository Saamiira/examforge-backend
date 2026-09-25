package com.examforge.api.assessment.generation;

import java.util.List;
import java.util.Map;

import com.examforge.api.assessment.entity.Assessment;
import com.examforge.api.assessment.entity.AssessmentStatus;
import com.examforge.api.assessment.entity.Question;
import com.examforge.api.assessment.entity.QuestionSource;
import com.examforge.api.assessment.mapper.QuestionMapper;
import com.examforge.api.assessment.repository.AssessmentRepository;
import com.examforge.api.common.exception.ResourceNotFoundException;
import com.examforge.api.document.repository.DocumentChunkRepository;
import com.examforge.api.notification.event.AssessmentGeneratedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persists the outcome of a generation in short transactions, so the slow AI calls never hold a database connection.
 */
@Component
@RequiredArgsConstructor
public class AssessmentGenerationWriter {

    private static final int MAX_FAILURE_REASON = 500;

    private final AssessmentRepository assessmentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final QuestionMapper questionMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void saveGenerated(Long assessmentId, List<GeneratedQuestion> generated, Map<Long, Double> relevanceByChunk) {
        Assessment assessment = assessmentRepository.findWithQuestionsById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment", assessmentId));

        for (GeneratedQuestion item : generated) {
            Question question = questionMapper.fromGenerated(item);
            question.replaceOptions(questionMapper.fromGeneratedOptions(item.options()));
            for (Long chunkId : item.sourceChunkIds()) {
                question.addSource(new QuestionSource(
                        documentChunkRepository.getReferenceById(chunkId),
                        relevanceByChunk.getOrDefault(chunkId, 0.0)));
            }
            assessment.addQuestion(question);
        }
        assessment.setStatus(AssessmentStatus.READY);
        assessment.setFailureReason(null);

        eventPublisher.publishEvent(new AssessmentGeneratedEvent(assessment.getId(), assessment.getAuthor().getId()));
    }

    @Transactional
    public void markFailed(Long assessmentId, String reason) {
        assessmentRepository.findById(assessmentId).ifPresent(assessment -> {
            assessment.setStatus(AssessmentStatus.FAILED);
            assessment.setFailureReason(truncate(reason));
        });
    }

    private String truncate(String reason) {
        String text = reason == null || reason.isBlank() ? "Generation failed" : reason;
        return text.length() <= MAX_FAILURE_REASON ? text : text.substring(0, MAX_FAILURE_REASON);
    }
}
