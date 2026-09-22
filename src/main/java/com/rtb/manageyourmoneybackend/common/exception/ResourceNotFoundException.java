package com.rtb.manageyourmoneybackend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a requested resource cannot be located.
 * Mapped to HTTP 404 so it can be thrown directly from the service layer
 * without requiring a dedicated {@code @ExceptionHandler} for the common case.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String entityName, Object id) {
        return new ResourceNotFoundException(entityName + " not found with id: " + id);
    }
}
