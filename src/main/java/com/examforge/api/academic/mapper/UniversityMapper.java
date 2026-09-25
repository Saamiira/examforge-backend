package com.examforge.api.academic.mapper;

import com.examforge.api.academic.dto.UniversityRequest;
import com.examforge.api.academic.dto.UniversityResponse;
import com.examforge.api.academic.entity.University;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UniversityMapper {
    University toEntity(UniversityRequest request);
    UniversityResponse toResponse(University entity);
    void updateEntityFromRequest(UniversityRequest request, @MappingTarget University entity);
}
