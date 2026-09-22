package com.rtb.manageyourmoneybackend.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an operation would violate a uniqueness rule that is enforced
 * at the application layer (e.g. a case-insensitive "unique name per user"
 * constraint). Mapped to HTTP 409.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
