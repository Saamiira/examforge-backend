package com.examforge.api.academic.dto;

import lombok.Data;

@Data
public class CourseResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
}
