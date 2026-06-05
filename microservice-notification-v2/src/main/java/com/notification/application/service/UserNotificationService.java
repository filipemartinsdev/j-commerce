package com.notification.application.service;

import com.notification.application.dto.UserNotificationResponse;
import com.notification.application.exception.UserNotificationHasAlreadyBeenViewedException;
import com.notification.application.service.mapper.UserNotificationMapper;
import com.notification.domain.entity.UserNotification;
import io.github.responsekit.core.PagedResponse;
import io.github.responsekit.quarkus.PagedResponseFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;

import java.util.UUID;

@ApplicationScoped
public class UserNotificationService {
    private final UserNotificationMapper userNotificationMapper;

    public UserNotificationService(UserNotificationMapper userNotificationMapper) {
        this.userNotificationMapper = userNotificationMapper;
    }

    public PagedResponse<UserNotificationResponse> getAllByUserId(UUID userId, int page, int size) {
        return PagedResponseFactory.fromQuery(
                UserNotification.findAllByUserId(userId, page, size),
                userNotificationMapper::toResponse
        );
    }

    public void view(UUID id, UUID userId) {
        UserNotification notification = UserNotification.findById(id);

        if (!notification.userId.equals(userId))
            throw new BadRequestException("Invalid user");

        if (notification.viewed)
            throw new UserNotificationHasAlreadyBeenViewedException("Notifiactino has already been viewed");

        notification.viewed = true;
        notification.persist();
    }
}
