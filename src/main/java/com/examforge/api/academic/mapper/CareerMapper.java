package com.examforge.api.academic.mapper;

import com.examforge.api.academic.dto.CareerRequest;
import com.examforge.api.academic.dto.CareerResponse;
import com.examforge.api.academic.entity.Career;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CareerMapper {
    
    @Mapping(target = "university", ignore = true)
    Career toEntity(CareerRequest request);
    
    @Mapping(source = "university.id", target = "universityId")
    @Mapping(source = "university.name", target = "universityName")
    CareerResponse toResponse(Career entity);
    
    @Mapping(target = "university", ignore = true)
    void updateEntityFromRequest(CareerRequest request, @MappingTarget Career entity);
}
