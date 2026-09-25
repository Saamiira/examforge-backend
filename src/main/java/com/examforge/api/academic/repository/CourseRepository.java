package com.examforge.api.academic.repository;

import com.examforge.api.academic.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
}
