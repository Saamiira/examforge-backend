package com.examforge.api.assessment.dto;

public record QuestionSourceResponse(
        Long chunkId,
        Long documentId,
        String documentTitle,
        Integer pageNumber,
        String excerpt,
        double relevanceScore
) {
}
