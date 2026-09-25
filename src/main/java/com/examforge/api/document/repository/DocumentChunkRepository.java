package com.examforge.api.document.repository;

import com.examforge.api.document.entity.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    @Query(value = """
      SELECT * FROM document_chunks
      WHERE document_id IN (:documentIds)
      ORDER BY embedding <=> CAST(:queryEmbedding AS vector)
      LIMIT :limit
      """, nativeQuery = true)
    List<DocumentChunk> findMostSimilar(@Param("documentIds") List<Long> documentIds, @Param("queryEmbedding") String queryEmbedding, @Param("limit") int limit);
}
