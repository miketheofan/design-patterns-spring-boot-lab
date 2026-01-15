package com.payment_processing_system.exceptions;

/**
 * Exception thrown when payment processing fails after validation.
 * Results in HTTP 500 Internal Server Error response.
 * Examples: Insufficient funds, network timeout, external API failure.
 */
public class PaymentProcessingException extends RuntimeException {
    
    public PaymentProcessingException(String message) {
        super(message);
    }

    public PaymentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
