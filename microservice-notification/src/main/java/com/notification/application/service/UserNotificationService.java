package com.notification.application.service;

import com.notification.application.dto.PagedResponse;
import com.notification.application.dto.UserNotificationResponse;
import com.notification.domain.entity.UserNotification;
import com.notification.infra.persistence.UserNotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserNotificationService {
    private final UserNotificationRepository userNotificationRepository;
    private final UserNotificationMapper userNotificationMapper;

    public UserNotificationService(UserNotificationRepository userNotificationRepository, UserNotificationMapper userNotificationMapper) {
        this.userNotificationRepository = userNotificationRepository;
        this.userNotificationMapper = userNotificationMapper;
    }

    public PagedResponse<UserNotificationResponse> getAll(UUID authenticatedUserId, Pageable pageable) {
        Page<UserNotification> page = userNotificationRepository.findAllByUserId(authenticatedUserId, pageable);
        return PagedResponse.<UserNotificationResponse>builder()
                .page(page.getNumber())
                .size(page.getSize())
                .isLast(page.isLast())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .content(page.getContent().stream()
                        .map(entity -> userNotificationMapper.toResponse(entity))
                        .toList()
                )
                .build();
    }
}
