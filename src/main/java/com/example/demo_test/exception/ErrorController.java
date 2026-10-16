package com.example.demo_test.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo_test.dto.ApiResponse;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class ErrorController {

        private static final Logger logger = LoggerFactory.getLogger(ErrorController.class);

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<String>> handleValidationException(MethodArgumentNotValidException ex) {
                String message = ex.getBindingResult().getFieldErrors().stream()
                                .map(error -> error.getDefaultMessage())
                                .findFirst()
                                .orElse("Validation failed");

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.<String>builder()
                                                .success(false)
                                                .message(message)
                                                .data(null)
                                                .build());
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ApiResponse<String>> handleConstraintViolationException(ConstraintViolationException ex) {
                String message = ex.getConstraintViolations().stream()
                                .map(violation -> violation.getMessage())
                                .findFirst()
                                .orElse("Validation failed");

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.<String>builder()
                                                .success(false)
                                                .message(message)
                                                .data(null)
                                                .build());
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiResponse<String>> handleAccessDenied(AccessDeniedException ex) {
                return error(HttpStatus.FORBIDDEN, "You do not have permission to access this resource");
        }

        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ApiResponse<String>> handleAuthentication(AuthenticationException ex) {
                return error(HttpStatus.UNAUTHORIZED, "Authentication required or token invalid");
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ApiResponse<String>> handleDataIntegrity(DataIntegrityViolationException ex) {
                return error(HttpStatus.CONFLICT,
                                "Operation conflicts with existing data (duplicate or still referenced)");
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiResponse<String>> handleUnreadable(HttpMessageNotReadableException ex) {
                return error(HttpStatus.BAD_REQUEST, "Malformed or missing request body");
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ApiResponse<String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
                return error(HttpStatus.BAD_REQUEST, "Invalid value for parameter '" + ex.getName() + "'");
        }

        // Fallback: keep Spring's own status codes (404, 405, 415, ...) and map the rest to 500
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<String>> handleGeneric(Exception ex) {
                if (ex instanceof ErrorResponse errorResponse) {
                        return error(HttpStatus.valueOf(errorResponse.getStatusCode().value()),
                                        errorResponse.getBody().getDetail());
                }
                logger.error("Unhandled exception", ex);
                return error(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
        }

        private ResponseEntity<ApiResponse<String>> error(HttpStatus status, String message) {
                return ResponseEntity.status(status)
                                .body(ApiResponse.<String>builder()
                                                .success(false)
                                                .message(message)
                                                .data(null)
                                                .build());
        }

        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<ApiResponse<String>> handleApiException(ResponseStatusException ex) {
                return ResponseEntity.status(ex.getStatusCode())
                                .body(ApiResponse.<String>builder()
                                                .success(false)
                                                .message(ex.getReason())
                                                .data(null)
                                                .build());
        }
}