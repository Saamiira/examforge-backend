package com.examforge.api.common.exception;

import org.springframework.http.HttpStatus;

public class DocumentProcessingException extends ApiException {

    public DocumentProcessingException(String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, message);
    }
}
