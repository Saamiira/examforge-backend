package com.examforge.api.document.repository;

import com.examforge.api.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
}
