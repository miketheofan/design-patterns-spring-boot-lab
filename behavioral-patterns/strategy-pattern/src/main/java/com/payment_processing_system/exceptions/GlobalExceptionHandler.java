package com.payment_processing_system.exceptions;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.payment_processing_system.domains.PaymentResponse;
import com.payment_processing_system.enums.TransactionStatus;

/**
 * Global exception handler for payment processing errors.
 * Converts exceptions into appropriate HTTP responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Handles validation errors from @Valid annotations.
     * Returns 400 Bad Request with list of validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<PaymentResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
            List<String> errors = ex.getBindingResult()
                .getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());
    
            PaymentResponse response = PaymentResponse.builder()
                .status(TransactionStatus.FAILED)
                .errors(errors)
                .build();
    
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    
    /**
     * Handles payment validation exceptions)
     * Returns 400 Bad Request with validation error message.
     */
    @ExceptionHandler(PaymentValidationException.class)
    public ResponseEntity<PaymentResponse> handlePaymentValidationException(PaymentValidationException ex) {
        PaymentResponse response = PaymentResponse.builder()
            .status(TransactionStatus.FAILED)
            .errors(List.of(ex.getMessage()))
            .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles unsupported payment method exceptions.
     * Returns 400 Bad Request.
     */
    @ExceptionHandler(UnsupportedPaymentMethodException.class)
    public ResponseEntity<PaymentResponse> handleUnsupportedPaymentMethod(
        UnsupportedPaymentMethodException ex
    ) {
        PaymentResponse response = PaymentResponse.builder()
            .status(TransactionStatus.FAILED)
            .errors(List.of(ex.getMessage()))
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Handles payment processing exceptions.
     * Returns 500 Internal Server Error.
     */
    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<PaymentResponse> handlePaymentProcessingException(
        PaymentProcessingException ex
    ) {
        PaymentResponse response = PaymentResponse.builder()
            .status(TransactionStatus.FAILED)
            .error("Payment processing failed: " + ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * Handles all other unexpected exceptions.
     * Returns 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<PaymentResponse> handleGenericException(Exception ex) {
        PaymentResponse response = PaymentResponse.builder()
            .status(TransactionStatus.FAILED)
            .error("An unexpected error occurred: " + ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
