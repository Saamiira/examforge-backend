package com.examforge.api.assessment.mapper;

import com.examforge.api.assessment.dto.AssessmentDetailResponse;
import com.examforge.api.assessment.dto.AssessmentSummaryResponse;
import com.examforge.api.assessment.dto.QuestionSourceResponse;
import com.examforge.api.assessment.entity.Assessment;
import com.examforge.api.assessment.entity.QuestionSource;
import com.examforge.api.document.entity.DocumentChunk;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AssessmentMapper {

    private static final int EXCERPT_LENGTH = 300;

    private final QuestionMapper questionMapper;

    public AssessmentSummaryResponse toSummary(Assessment assessment) {
        return new AssessmentSummaryResponse(
                assessment.getId(),
                assessment.getTitle(),
                assessment.getDifficulty(),
                assessment.getStatus(),
                assessment.getVisibility(),
                assessment.getQuestions().size(),
                assessment.getCourse().getId(),
                assessment.getAuthor().getId(),
                assessment.getCreatedAt());
    }

    public AssessmentDetailResponse toDetail(Assessment assessment) {
        return new AssessmentDetailResponse(
                assessment.getId(),
                assessment.getTitle(),
                assessment.getDescription(),
                assessment.getDifficulty(),
                assessment.getStatus(),
                assessment.getFailureReason(),
                assessment.getVisibility(),
                assessment.getCourse().getId(),
                assessment.getAuthor().getId(),
                assessment.getCreatedAt(),
                questionMapper.toResponses(assessment.getQuestions()));
    }

    public QuestionSourceResponse toSourceResponse(QuestionSource source) {
        DocumentChunk chunk = source.getChunk();
        String content = chunk.getContent();
        String excerpt = content.length() <= EXCERPT_LENGTH ? content : content.substring(0, EXCERPT_LENGTH) + "...";
        return new QuestionSourceResponse(
                chunk.getId(),
                chunk.getDocument().getId(),
                chunk.getDocument().getTitle(),
                chunk.getPageNumber(),
                excerpt,
                source.getRelevanceScore());
    }
}
