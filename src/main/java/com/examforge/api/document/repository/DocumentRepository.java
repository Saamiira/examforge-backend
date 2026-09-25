package com.examforge.api.document.repository;

import com.examforge.api.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;



@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByCourseIdOrderByCreatedAtDesc(Long courseId);

    List<Document> findByIdInAndCourseIdAndStatus(java.util.Collection<Long> ids, Long courseId, com.examforge.api.document.entity.DocumentStatus status);

}
