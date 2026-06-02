package com.notification.application.dto;

import java.time.Instant;
import java.util.UUID;

public record UserNotificationResponse(
        UUID id,
        String title,
        String description,
        String category,
        boolean viewed,
        Instant createdAt
) {
}
