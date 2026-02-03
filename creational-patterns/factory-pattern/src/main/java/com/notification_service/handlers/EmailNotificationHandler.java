package com.notification_service.handlers;

import com.notification_service.domains.NotificationRequest;
import com.notification_service.domains.NotificationResult;
import com.notification_service.domains.ValidationResult;
import com.notification_service.enums.NotificationChannel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class EmailNotificationHandler implements NotificationHandler {

    private static final String EMAIL_FORMAT = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final int SUBJECT_MAX_LENGTH = 200;
    private static final int MESSAGE_MAX_LENGTH = 10000;

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
