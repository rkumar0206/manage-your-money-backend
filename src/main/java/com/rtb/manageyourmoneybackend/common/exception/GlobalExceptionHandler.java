package com.rtb.manageyourmoneybackend.common.exception;

import com.google.firebase.auth.FirebaseAuthException;
import com.rtb.manageyourmoneybackend.common.model.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FirebaseAuthException.class)
    public ResponseEntity<ErrorResponse> handleFirebaseAuthException(FirebaseAuthException e) {

        logException(e);

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

        logException(e);

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

        logException(e);

        ErrorResponse errorResponse = new ErrorResponse(
                e.getMessage(),
                e.getMessage(),
                HttpStatus.CONFLICT.value(),
                Instant.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {

        logException(ex);

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabledException(DisabledException ex) {

        logException(ex);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(
                "Please verify your email before logging in.",
                ex.getMessage(),
                HttpStatus.FORBIDDEN.value(),
                Instant.now()
                )
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {

        logException(ex);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(
                        "Invalid username or password.",
                        ex.getMessage(),
                        HttpStatus.UNAUTHORIZED.value(),
                        Instant.now()
                ));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {

        logException(ex);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(
                        "Authentication failed.",
                        ex.getMessage(),
                        HttpStatus.UNAUTHORIZED.value(),
                        Instant.now()
                ));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(AuthorizationDeniedException ex) {

        logException(ex);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(
                        "Authentication failed.",
                        ex.getMessage(),
                        HttpStatus.UNAUTHORIZED.value(),
                        Instant.now()
                ));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOtherException(Exception e) {

        logException(e);

        ErrorResponse errorResponse = new ErrorResponse(
                e.getMessage(),
                e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                Instant.now()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void logException(Exception ex) {
        log.error(ex.getMessage(), ex);
    }
}
