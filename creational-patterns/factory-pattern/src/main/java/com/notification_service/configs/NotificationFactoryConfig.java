package com.notification_service.configs;

import com.notification_service.enums.NotificationChannel;
import com.notification_service.handlers.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class NotificationFactoryConfig {
    @Bean
    public Map<NotificationChannel, NotificationHandler> notificationHandlers(
            EmailNotificationHandler emailHandler,
            SmsNotificationHandler smsHandler,
            PushNotificationHandler pushHandler,
            SlackNotificationHandler slackHandler
    ) {
        return Map.of(
                NotificationChannel.EMAIL, emailHandler,
                NotificationChannel.SMS, smsHandler,
                NotificationChannel.PUSH, pushHandler,
                NotificationChannel.SLACK, slackHandler
        );
    }
}
