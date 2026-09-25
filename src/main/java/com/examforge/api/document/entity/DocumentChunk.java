package com.examforge.api.document.entity;

import com.examforge.api.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "document_chunks", indexes = @Index(name = "idx_document_chunks_document", columnList = "document_id"))
public class DocumentChunk extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private Integer pageNumber;

    @Column(nullable = false)
    private int chunkIndex;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 3072)
    @Column(nullable = false)
    private float[] embedding;
}
