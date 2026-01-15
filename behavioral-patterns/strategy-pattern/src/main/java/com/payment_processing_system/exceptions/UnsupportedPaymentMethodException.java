package com.payment_processing_system.exceptions;

/**
 * Exception thrown when an unsupported payment method is requested.
 * Results in HTTP 400 Bad Request response.
 */
public class UnsupportedPaymentMethodException extends RuntimeException {

    public UnsupportedPaymentMethodException(String message) {
        super(message);
    }
}
