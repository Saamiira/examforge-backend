package com.examforge.api.assessment.repository;

import java.util.Optional;

import com.examforge.api.assessment.entity.Assessment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    @EntityGraph(attributePaths = "questions")
    Optional<Assessment> findWithQuestionsById(Long id);
}
