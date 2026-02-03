package com.notification_service.utils;

import com.notification_service.exceptions.NotificationProcessingException;
import com.notification_service.exceptions.ValidationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Shared validation utilities for notification handlers.
 * Provides common validation logic to avoid code duplication.
 */
@Component
public class NotificationValidationHelper  {
    private static final String NOTIFICATION_ID_PREFIX = "NOTIF-";

    /**
     * Extracts a required string from notification metadata map.
     * @throws ValidationException if field is missing or blank
     */
    public String getSpecificKey(Map<String, Object> details, String key) {
        var value = details.get(key);
        if (value == null || value.toString().isBlank()) {
            throw new ValidationException(List.of(key + " is required"));
        }
        return value.toString();
    }

    /**
     * Validates a string against a regex pattern.
     * @throws ValidationException if pattern doesn't match
     */
    public void validatePattern(String value, String pattern, String errorMessage) {
        if (!value.matches(pattern)) {
            throw new ValidationException(List.of(errorMessage));
        }
    }

    /**
     * Generates a unique notification id.
     */
    public String generateNotificationId() {
        return NOTIFICATION_ID_PREFIX + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Simulates random processing failure.
     * @throws NotificationProcessingException randomly based on failure rate
     */
    public void simulateRandomFailure(double failureRate, String errorMessage) {
        if (Math.random() < failureRate) {
            throw new NotificationProcessingException(errorMessage);
        }
    }
}
