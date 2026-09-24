package com.examforge.api.assessment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OptionRequest(
        @NotBlank @Size(max = 500) String text,
        boolean correct
) {
}
