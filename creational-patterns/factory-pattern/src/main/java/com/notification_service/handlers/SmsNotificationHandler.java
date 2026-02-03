package com.notification_service.handlers;

import com.notification_service.domains.NotificationRequest;
import com.notification_service.domains.NotificationResult;
import com.notification_service.domains.ValidationResult;
import com.notification_service.enums.NotificationChannel;

import java.math.BigDecimal;

public class SmsNotificationHandler implements NotificationHandler {
    @Override
    public ValidationResult validate(NotificationRequest request) {
        return null;
    }

    @Override
    public NotificationResult send(NotificationRequest request) {
        return null;
    }

    @Override
    public BigDecimal calculateCost(NotificationRequest request) {
        return null;
    }

    @Override
    public NotificationChannel getSupportedChannel() {
        return null;
    }
}
