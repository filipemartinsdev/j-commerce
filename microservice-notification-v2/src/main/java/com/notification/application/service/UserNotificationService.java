package com.notification.application.service;

import com.notification.application.dto.UserNotificationResponse;
import com.notification.application.exception.UserNotificationHasAlreadyBeenViewedException;
import com.notification.application.service.mapper.UserNotificationMapper;
import com.notification.domain.entity.UserNotification;
import com.notification.domain.entity.UserNotificationCategory;
import io.github.responsekit.core.PagedResponse;
import io.github.responsekit.quarkus.PagedResponseFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.BadRequestException;

import java.util.UUID;

// TODO: unit tests
@ApplicationScoped
public class UserNotificationService {
    private final UserNotificationMapper userNotificationMapper;
    private final EntityManager entityManager;

    public UserNotificationService(UserNotificationMapper userNotificationMapper, EntityManager entityManager) {
        this.userNotificationMapper = userNotificationMapper;
        this.entityManager = entityManager;
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

    public void create(UUID userId, String title, String description, Long categoryId){
        UserNotification notification = new UserNotification();
        notification.userId = userId;
        notification.title = title;
        notification.description = description;
        notification.category = entityManager.getReference(UserNotificationCategory.class, categoryId);
        notification.persist();
    }
}
