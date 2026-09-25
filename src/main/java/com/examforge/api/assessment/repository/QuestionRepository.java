package com.examforge.api.assessment.repository;

import com.examforge.api.assessment.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}
