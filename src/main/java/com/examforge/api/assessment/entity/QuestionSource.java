package com.examforge.api.assessment.entity;

import com.examforge.api.common.entity.BaseEntity;
import com.examforge.api.document.entity.DocumentChunk;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Traceability link between a generated question and the document fragment that supports it.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "question_sources",
        uniqueConstraints = @UniqueConstraint(name = "uk_question_sources_question_chunk",
                columnNames = {"question_id", "chunk_id"}),
        indexes = @Index(name = "idx_question_sources_question", columnList = "question_id"))
public class QuestionSource extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chunk_id", nullable = false)
    private DocumentChunk chunk;

    @Column(nullable = false)
    private double relevanceScore;

    public QuestionSource(DocumentChunk chunk, double relevanceScore) {
        this.chunk = chunk;
        this.relevanceScore = relevanceScore;
    }
}
