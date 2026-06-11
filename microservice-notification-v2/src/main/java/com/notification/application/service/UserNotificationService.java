package com.notification.application.service;

import com.notification.application.dto.UserNotificationResponse;
import com.notification.application.exception.UserNotificationHasAlreadyBeenViewedException;
import com.notification.application.exception.UserNotificationNotFoundException;
import com.notification.application.service.mapper.UserNotificationMapper;
import com.notification.domain.entity.UserNotification;
import com.notification.domain.entity.UserNotificationCategory;
import com.notification.infra.persistence.UserNotificationRepository;
import io.github.responsekit.core.PagedResponse;
import io.github.responsekit.quarkus.PagedResponseFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;

import java.util.UUID;

@ApplicationScoped
public class UserNotificationService {
    private final UserNotificationRepository userNotificationRepository;
    private final UserNotificationMapper userNotificationMapper;
    private final EntityManager entityManager;

    public UserNotificationService(UserNotificationRepository userNotificationRepository, UserNotificationMapper userNotificationMapper, EntityManager entityManager) {
        this.userNotificationRepository = userNotificationRepository;
        this.userNotificationMapper = userNotificationMapper;
        this.entityManager = entityManager;
    }

    public PagedResponse<UserNotificationResponse> getAllByUserId(UUID userId, int page, int size) {
        return PagedResponseFactory.fromQuery(
                userNotificationRepository.findAllByUserId(userId, page, size),
                userNotificationMapper::toResponse
        );
    }

    public void view(UUID id, UUID userId) {
        UserNotification notification = userNotificationRepository.findById(id);

        if (notification == null)
            throw new UserNotificationNotFoundException("Notification not found by ID: "+id);

        if (!notification.userId.equals(userId))
            throw new ForbiddenException();

        if (notification.viewed)
            throw new UserNotificationHasAlreadyBeenViewedException("Notification has already been viewed");

        notification.viewed = true;

        userNotificationRepository.persist(notification);
    }

    public void create(UUID userId, String title, String description, Long categoryId){
        UserNotification notification = new UserNotification();
        notification.userId = userId;
        notification.title = title;
        notification.description = description;
        notification.category = entityManager.getReference(UserNotificationCategory.class, categoryId);

        userNotificationRepository.persist(notification);
    }
}
