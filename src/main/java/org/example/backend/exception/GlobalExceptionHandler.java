package org.example.backend.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.backend.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        @ExceptionHandler(DuplicateResourceException.class)
        public ResponseEntity<ApiResponse<Void>> handleDuplicateResource(
                        DuplicateResourceException ex, WebRequest request) {

                log.error("Duplicate resource: {}", ex.getMessage());

                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ApiResponse.error(ex.getMessage(), "DUPLICATE_RESOURCE"));
        }

        @ExceptionHandler(InvalidCredentialsException.class)
        public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(
                        InvalidCredentialsException ex, WebRequest request) {

                log.error("Invalid credentials: {}", ex.getMessage());

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(ApiResponse.error(ex.getMessage(), "INVALID_CREDENTIALS"));
        }

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
                        ResourceNotFoundException ex, WebRequest request) {

                log.error("Resource not found: {}", ex.getMessage());

                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.error(ex.getMessage(), "RESOURCE_NOT_FOUND"));
        }

        @ExceptionHandler(InvalidOperationException.class)
        public ResponseEntity<ApiResponse<Void>> handleInvalidOperation(
                        InvalidOperationException ex, WebRequest request) {

                log.error("Invalid operation: {}", ex.getMessage());

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(ex.getMessage(), "INVALID_OPERATION"));
        }

        @ExceptionHandler(InsufficientStockException.class)
        public ResponseEntity<ApiResponse<Void>> handleInsufficientStock(
                        InsufficientStockException ex, WebRequest request) {

                log.error("Insufficient stock: {}", ex.getMessage());

                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ApiResponse.error(ex.getMessage(), "INSUFFICIENT_STOCK"));
        }

        @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
        public ResponseEntity<ApiResponse<Void>> handleOptimisticLock(
                        org.springframework.orm.ObjectOptimisticLockingFailureException ex, WebRequest request) {

                log.error("Concurrent modification detected: {}", ex.getMessage());

                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ApiResponse.error("Concurrent modification detected. Please retry.",
                                                "OPTIMISTIC_LOCK_ERROR"));
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ApiResponse<Void>> handleBadCredentials(
                        BadCredentialsException ex, WebRequest request) {

                log.error("Bad credentials: {}", ex.getMessage());

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(ApiResponse.error("Invalid username or password", "BAD_CREDENTIALS"));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
                        MethodArgumentNotValidException ex) {

                Map<String, String> errors = new HashMap<>();
                ex.getBindingResult().getAllErrors().forEach((error) -> {
                        String fieldName = ((FieldError) error).getField();
                        String errorMessage = error.getDefaultMessage();
                        errors.put(fieldName, errorMessage);
                });

                log.error("Validation errors: {}", errors);

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.<Map<String, String>>builder()
                                                .success(false)
                                                .message("Validation failed")
                                                .data(errors)
                                                .errorCode("VALIDATION_ERROR")
                                                .timestamp(java.time.LocalDateTime.now())
                                                .build());
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<Void>> handleGenericException(
                        Exception ex, WebRequest request) {

                log.error("Unexpected error: {}", ex.getMessage(), ex);

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.error("An unexpected error occurred", "INTERNAL_ERROR"));
        }
}
