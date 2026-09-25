package com.examforge.api.common.exception;

import org.springframework.http.HttpStatus;

public class AiGenerationException extends ApiException {

    public AiGenerationException(String message) {
        super(HttpStatus.BAD_GATEWAY, message);
    }
}
