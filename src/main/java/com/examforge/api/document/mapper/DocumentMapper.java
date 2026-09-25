package com.examforge.api.document.mapper;

import com.examforge.api.document.dto.DocumentResponse;
import com.examforge.api.document.entity.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DocumentMapper {

    @Mapping(source = "course.id", target = "courseId")
    @Mapping(source = "user.id", target = "userId")
    DocumentResponse toResponse(Document entity);
}
