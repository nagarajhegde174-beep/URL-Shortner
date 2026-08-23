package com.urlshortener.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class FreePlanLimitException extends RuntimeException {
    public FreePlanLimitException(String message) {
        super(message);
    }
}
