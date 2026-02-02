package com.notification_service.domains;

import com.notification_service.enums.NotificationStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response returned after sending a notification.
 */
@Builder
public record NotificationResult(String notificationId, NotificationStatus status, String channelUsed,
                                 LocalDateTime sentAt, String providerReference, BigDecimal cost, String errorMessage) {
}
