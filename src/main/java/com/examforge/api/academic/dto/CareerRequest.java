package com.examforge.api.academic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CareerRequest {
    @NotBlank
    @Size(max = 150)
    private String name;

    @Size(max = 20)
    private String code;

    @NotNull
    private Long universityId;
}
