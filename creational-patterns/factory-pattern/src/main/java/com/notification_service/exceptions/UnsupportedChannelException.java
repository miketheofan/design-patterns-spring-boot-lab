package com.notification_service.exceptions;

/**
 * Thrown when an unsupported notification channel is requested.
 * Results in HTTP 400 Bad Request response.
 */
public class UnsupportedChannelException extends RuntimeException {
    public UnsupportedChannelException(String message) {
        super(message);
    }
}
