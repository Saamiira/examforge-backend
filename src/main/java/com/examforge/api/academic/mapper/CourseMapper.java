package com.examforge.api.academic.mapper;

import com.examforge.api.academic.dto.CourseRequest;
import com.examforge.api.academic.dto.CourseResponse;
import com.examforge.api.academic.entity.Course;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CourseMapper {
    Course toEntity(CourseRequest request);
    CourseResponse toResponse(Course entity);
    void updateEntityFromRequest(CourseRequest request, @MappingTarget Course entity);
}
