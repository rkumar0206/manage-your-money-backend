package com.rtb.manageyourmoneybackend.common.exception;

import com.google.firebase.auth.FirebaseAuthException;
import com.rtb.manageyourmoneybackend.common.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FirebaseAuthException.class)
    public ResponseEntity<ErrorResponse> handleFirebaseAuthException(FirebaseAuthException e) {

        ErrorResponse errorResponse = new ErrorResponse(
                "Authentication failed",
                e.getMessage(),
                HttpStatus.UNAUTHORIZED.value(),
                Instant.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException e) {

        ErrorResponse errorResponse = new ErrorResponse(
                e.getMessage(),
                e.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                Instant.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(DuplicateResourceException e) {

        ErrorResponse errorResponse = new ErrorResponse(
                e.getMessage(),
                e.getMessage(),
                HttpStatus.CONFLICT.value(),
                Instant.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOtherException(Exception e) {

        ErrorResponse errorResponse = new ErrorResponse(
                e.getMessage(),
                e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                Instant.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
