package com.examforge.api.academic.repository;

import com.examforge.api.academic.entity.Career;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CareerRepository extends JpaRepository<Career, Long> {
}
