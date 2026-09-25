package com.examforge.api.assessment.repository;

import java.util.List;

import com.examforge.api.assessment.entity.QuestionSource;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionSourceRepository extends JpaRepository<QuestionSource, Long> {

    @EntityGraph(attributePaths = {"chunk", "chunk.document"})
    List<QuestionSource> findByQuestionIdOrderByRelevanceScoreDesc(Long questionId);
}
