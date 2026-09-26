package com.examforge.api.academic.service;

import com.examforge.api.academic.dto.CourseRequest;
import com.examforge.api.academic.dto.CourseResponse;
import com.examforge.api.academic.entity.Course;
import com.examforge.api.academic.entity.UserCourse;
import com.examforge.api.academic.mapper.CourseMapper;
import com.examforge.api.academic.repository.CourseRepository;
import com.examforge.api.academic.repository.UserCourseRepository;
import com.examforge.api.common.exception.DuplicateResourceException;
import com.examforge.api.common.exception.ResourceNotFoundException;
import com.examforge.api.user.entity.User;
import com.examforge.api.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final UserCourseRepository userCourseRepository;
    private final CourseMapper courseMapper;
    private final CurrentUserProvider currentUserProvider;

    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(courseMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        return courseMapper.toResponse(course);
    }

    @Transactional
    public CourseResponse createCourse(CourseRequest request) {
        Course course = courseMapper.toEntity(request);
        return courseMapper.toResponse(courseRepository.save(course));
    }

    @Transactional
    public CourseResponse updateCourse(Long id, CourseRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        
        courseMapper.updateEntityFromRequest(request, course);
        return courseMapper.toResponse(courseRepository.save(course));
    }

    @Transactional
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new ResourceNotFoundException("Course not found");
        }
        courseRepository.deleteById(id);
    }

    @Transactional
    public void enrollCurrentUser(Long courseId) {
        User currentUser = currentUserProvider.getCurrentUser();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
                
        if (userCourseRepository.existsByUserIdAndCourseId(currentUser.getId(), courseId)) {
            throw new DuplicateResourceException("User is already enrolled in this course");
        }
        
        UserCourse userCourse = new UserCourse();
        userCourse.setUser(currentUser);
        userCourse.setCourse(course);
        userCourseRepository.save(userCourse);
    }
}
