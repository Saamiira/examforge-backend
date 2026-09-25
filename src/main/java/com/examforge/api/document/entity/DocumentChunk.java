package com.examforge.api.document.entity;

import com.examforge.api.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Array;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "document_chunks")
public class DocumentChunk extends BaseEntity {

    @Column(columnDefinition = "TEXT")
    private String content;

    private Integer pageNumber;
    private int chunkIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 3072)
    private float[] embedding;
}
