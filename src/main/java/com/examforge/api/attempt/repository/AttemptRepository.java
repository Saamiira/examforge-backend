package com.examforge.api.attempt.repository;

import java.util.Optional;

import com.examforge.api.attempt.entity.Attempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {

    @EntityGraph(attributePaths = "assessment")
    Page<Attempt> findByStudentIdOrderByCreatedAtDesc(Long studentId, Pageable pageable);

    @EntityGraph(attributePaths = {"assessment", "student", "answers"})
    Optional<Attempt> findWithAssessmentById(Long id);
}
