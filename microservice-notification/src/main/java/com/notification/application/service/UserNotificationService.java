package com.notification.application.service;

import com.notification.application.dto.PagedResponse;
import com.notification.application.dto.UserNotificationResponse;
import com.notification.application.exception.UserNotificationHasAlreadyBeenReadException;
import com.notification.application.exception.UserNotificationNotFoundException;
import com.notification.application.message.NotifyPaymentConfirmedMessage;
import com.notification.application.message.NotifyPaymentGeneratedMessage;
import com.notification.application.service.mapper.UserNotificationMapper;
import com.notification.domain.entity.UserNotification;
import com.notification.domain.entity.UserNotificationCategory;
import com.notification.infra.persistence.UserNotificationCategoryRepository;
import com.notification.infra.persistence.UserNotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserNotificationService {
    private final UserNotificationRepository userNotificationRepository;
    private final UserNotificationMapper userNotificationMapper;
    private final UserNotificationCategoryRepository userNotificationCategoryRepository;

    public UserNotificationService(UserNotificationRepository userNotificationRepository, UserNotificationMapper userNotificationMapper, UserNotificationCategoryRepository userNotificationCategoryRepository) {
        this.userNotificationRepository = userNotificationRepository;
        this.userNotificationMapper = userNotificationMapper;
        this.userNotificationCategoryRepository = userNotificationCategoryRepository;
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

    public void markAsRead(UUID id, UUID userId) {
        UserNotification notification = userNotificationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new UserNotificationNotFoundException("User notification not found with ID: "+id));

        if (notification.getIsRead())
            throw new UserNotificationHasAlreadyBeenReadException("User notification has already been read");

        notification.setIsRead(true);
        userNotificationRepository.save(notification);
    }

    public void notifyPaymentGenerated(NotifyPaymentGeneratedMessage message) {
        UserNotification notification = new UserNotification();
        notification.setUserId(message.userId());
        notification.setCategory(
                userNotificationCategoryRepository.getReferenceById(UserNotificationCategory.Value.PURCHASE.getId())
        );
        notification.setTitle("Payment Generated");
        notification.setDescription("Your payment of R$"+message.value()+" has been generated and sent by email");

        userNotificationRepository.save(notification);
    }

    public void notifyPaymentConfirmed(NotifyPaymentConfirmedMessage message) {
        UserNotification notification = new UserNotification();
        notification.setUserId(message.userId());
        notification.setCategory(
                userNotificationCategoryRepository.getReferenceById(UserNotificationCategory.Value.PURCHASE.getId())
        );
        notification.setTitle("Payment Confirmed");
        notification.setDescription("Your payment of R$"+message.value()+" has been confirmed. Your order will be shipped soon");

        userNotificationRepository.save(notification);
    }
}
