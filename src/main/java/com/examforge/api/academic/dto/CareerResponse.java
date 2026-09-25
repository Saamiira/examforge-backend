package com.examforge.api.academic.dto;

import lombok.Data;

@Data
public class CareerResponse {
    private Long id;
    private String name;
    private String code;
    private Long universityId;
    private String universityName;
}
