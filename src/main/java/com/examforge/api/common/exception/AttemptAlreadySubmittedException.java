package com.examforge.api.common.exception;

import org.springframework.http.HttpStatus;

public class AttemptAlreadySubmittedException extends ApiException {

    public AttemptAlreadySubmittedException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
