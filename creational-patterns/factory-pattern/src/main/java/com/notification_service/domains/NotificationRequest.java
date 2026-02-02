package com.notification_service.domains;

import com.notification_service.enums.NotificationChannel;
import com.notification_service.enums.Priority;
import lombok.Builder;

import java.util.Map;

/**
 * Request for sending a notification through a specific channel.
 */
@Builder
public record NotificationRequest(String recipient, String subject, String message, NotificationChannel channel,
                                  Map<String, String> metadata, Priority priority) {
}
