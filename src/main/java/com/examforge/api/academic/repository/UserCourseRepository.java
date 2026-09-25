package com.examforge.api.academic.repository;

import com.examforge.api.academic.entity.UserCourse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCourseRepository extends JpaRepository<UserCourse, Long> {
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
}
