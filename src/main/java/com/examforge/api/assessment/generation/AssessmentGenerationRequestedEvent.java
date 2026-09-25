package com.examforge.api.assessment.generation;

import java.util.List;

import com.examforge.api.assessment.dto.AssessmentGenerateRequest;

/**
 * Published when an assessment is created in GENERATING status; processed after commit, off the request thread.
 */
public record AssessmentGenerationRequestedEvent(
        Long assessmentId,
        List<Long> documentIds,
        AssessmentGenerateRequest request
) {
}
