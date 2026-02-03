package com.notification_service.factories;

import com.notification_service.enums.NotificationChannel;
import com.notification_service.exceptions.UnsupportedChannelException;
import com.notification_service.handlers.NotificationHandler;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@AllArgsConstructor
public class NotificationFactory {
    private final Map<NotificationChannel, NotificationHandler> handlers;

    public NotificationHandler createHandler(NotificationChannel channel) {
        NotificationHandler handler = handlers.get(channel);
        if (handler == null) {
            throw new UnsupportedChannelException("Channel not supported: " + channel);
        }
        return handler;
    }
}
