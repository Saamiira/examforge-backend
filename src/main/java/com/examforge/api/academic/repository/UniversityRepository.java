package com.examforge.api.academic.repository;

import com.examforge.api.academic.entity.University;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UniversityRepository extends JpaRepository<University, Long> {
    boolean existsByName(String name);
}
