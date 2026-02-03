package com.notification_service.exceptions;

/**
 * Thrown when notification delivery fails after validation.
 * Results in HTTP 500 Internal Server Error response.
 */
public class NotificationProcessingException extends RuntimeException {
    public NotificationProcessingException(String message) {
        super(message);
    }

    public NotificationProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
