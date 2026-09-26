package com.examforge.api.assessment.repository;

import java.util.Collection;
import java.util.Optional;

import com.examforge.api.assessment.entity.Assessment;
import com.examforge.api.assessment.entity.AssessmentStatus;
import com.examforge.api.assessment.entity.Visibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    @EntityGraph(attributePaths = "questions")
    Optional<Assessment> findWithQuestionsById(Long id);

    Page<Assessment> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);

    Page<Assessment> findByCourseIdAndStatusAndVisibilityInOrderByCreatedAtDesc(
            Long courseId, AssessmentStatus status, Collection<Visibility> visibilities, Pageable pageable);
}
