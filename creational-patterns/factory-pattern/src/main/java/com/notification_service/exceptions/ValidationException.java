package com.notification_service.exceptions;

import lombok.Getter;

import java.util.List;

/**
 * Thrown when a notification request fails validation.
 * Results in HTTP 400 Bad Request response.
 */
@Getter
public class ValidationException extends RuntimeException {
    private final List<String> errors;

    public ValidationException(List<String> errors) {
        super(String.join(", ", errors));
        this.errors = errors;
    }
}
