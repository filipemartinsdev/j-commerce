package com.notification.application.service.mapper;

import com.notification.application.dto.UserNotificationResponse;
import com.notification.domain.entity.UserNotification;
import io.vertx.ext.auth.User;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserNotificationMapper {
    public UserNotificationResponse toResponse(UserNotification entity) {
        return new UserNotificationResponse(
                entity.id,
                entity.title,
                entity.description,
                entity.category.name,
                entity.viewed,
                entity.createdAt
        );
    }
}
