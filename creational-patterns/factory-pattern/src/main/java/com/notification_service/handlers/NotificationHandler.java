package com.notification_service.handlers;

import com.notification_service.domains.NotificationRequest;
import com.notification_service.domains.NotificationResult;
import com.notification_service.domains.ValidationResult;
import com.notification_service.enums.NotificationChannel;

import java.math.BigDecimal;

/**
 * Handler for sending notifications through a specific channel.
 *
 * <p>
 * Implementations must provide channel-specific validation,
 * delivery logic, and cost calculation.
 * </p>
 */
public interface NotificationHandler {
    /** Validates the notification request for this channel. */
    ValidationResult validate(NotificationRequest request);

    /** Sends the notification through this channel. */
    NotificationResult send(NotificationRequest request);

    /** Calculates the cost for sending this notification. */
    BigDecimal calculateCost(NotificationRequest request);

    /** Returns the channel this handler supports. */
    NotificationChannel getSupportedChannel();
}
