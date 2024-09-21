package com.fordevs.querybridge.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler to handle all exceptions and provide a generic error response.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles all exceptions and provides a generic error response.
     *
     * @param e the exception
     * @return ResponseEntity with error message and status code
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        // Log the error and return a generic error response
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error connecting to the database: " + e.getMessage());
    }
}