package com.payment_processing_system.exceptions;

/**
 * Exception thrown when payment details fail validation.
 * Results in HTTP 400 Bad Request response.
 */
public class PaymentValidationException extends RuntimeException {

    public PaymentValidationException(String message) {
        super(message);
    }

    public PaymentValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
