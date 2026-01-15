package com.payment_processing_system.utils;

import java.util.Map;
import java.util.UUID;

import com.payment_processing_system.exceptions.PaymentProcessingException;
import com.payment_processing_system.exceptions.PaymentValidationException;
import org.springframework.stereotype.Component;

/**
 * Shared validation utilities for payment strategies.
 * Provides common validation logic to avoid code duplication.
 */
@Component
public class PaymentValidationHelper {

    private final static String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private final static String EMAIL_VALIDATION_ERROR_MSG = "Invalid email format";

    private final static String TRANSACTION_ID_PREFIX = "TXN-";

    /**
     * Extracts a required string from payment details map.
     * @throws PaymentValidationException if field is missing or blank
     */
    public String getSpecificKey(Map<String, Object> details, String key) {
        var value = details.get(key);
        if (value == null || value.toString().isBlank()) {
            throw new PaymentValidationException(key + " is required");
        }
        return value.toString();
    }

    /**
     * Validates a string against a regex pattern.
     * @throws PaymentValidationException if pattern doesn't match
     */
    public void validatePattern(String value, String pattern, String errorMessage) {
        if (!value.matches(pattern)) {
            throw new PaymentValidationException(errorMessage);
        }
    }

    /** 
     * Validates email format.
     * @throws PaymentValidationException if invalid email
     */
    public void validateEmail(String email) {
        if (!email.matches(EMAIL_PATTERN)) {
            throw new PaymentValidationException(EMAIL_VALIDATION_ERROR_MSG);
        }
    }

    /**
     * Generates a unique transaction ID.
     */
    public String generateTransactionId(){
        return TRANSACTION_ID_PREFIX + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /** 
     * Simulates random processing failure.
     * @throws PaymentProcessingException randomly based on failure rate
     */
    public void simulateRandomFailure(double failureRate, String errorMessage) {
        if (Math.random() < failureRate) {
            throw new PaymentProcessingException(errorMessage);
        }
    }
}
