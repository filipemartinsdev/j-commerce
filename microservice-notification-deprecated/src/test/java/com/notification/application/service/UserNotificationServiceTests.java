package com.notification.application.service;

import com.notification.application.dto.PagedResponse;
import com.notification.application.dto.UserNotificationResponse;
import com.notification.application.exception.UserNotificationHasAlreadyBeenReadException;
import com.notification.application.exception.UserNotificationNotFoundException;
import com.notification.application.message.NotifyOrderCancelledMessage;
import com.notification.application.message.NotifyPaymentConfirmedMessage;
import com.notification.application.message.NotifyPaymentGeneratedMessage;
import com.notification.application.service.mapper.UserNotificationMapper;
import java.math.BigDecimal;
import com.notification.domain.entity.UserNotification;
import com.notification.domain.entity.UserNotificationCategory;
import com.notification.infra.persistence.UserNotificationCategoryRepository;
import com.notification.infra.persistence.UserNotificationRepository;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserNotificationServiceTests {

    @Mock
    private UserNotificationRepository userNotificationRepository;

    @Mock
    private UserNotificationMapper userNotificationMapper;

    @Mock
    private UserNotificationCategoryRepository userNotificationCategoryRepository;

    @InjectMocks
    private UserNotificationService userNotificationService;

    @Test
    @DisplayName("Should return paginated notifications")
    void getAllTestCase1() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        UserNotification notification = new UserNotification();
        notification.setId(UUID.randomUUID());
        notification.setUserId(userId);
        notification.setTitle("Test Title");

        Page<UserNotification> page = new PageImpl<>(List.of(notification), pageable, 1);

        UserNotificationResponse response = new UserNotificationResponse(
                notification.getId(),
                "Test Title",
                "Test Description",
                "PURCHASE",
                false,
                Instant.now()
        );

        when(userNotificationRepository.findAllByUserId(userId, pageable))
                .thenReturn(page);
        when(userNotificationMapper.toResponse(notification))
                .thenReturn(response);

        PagedResponse<UserNotificationResponse> result = userNotificationService.getAll(userId, pageable);

        assertNotNull(result);
        assertEquals(1, result.totalElements());
        assertEquals("Test Title", result.content().get(0).title());
        verify(userNotificationRepository).findAllByUserId(userId, pageable);
    }

    @Test
    @DisplayName("Should return empty page when no notifications")
    void getAllTestCase2() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        Page<UserNotification> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(userNotificationRepository.findAllByUserId(userId, pageable))
                .thenReturn(emptyPage);

        PagedResponse<UserNotificationResponse> result = userNotificationService.getAll(userId, pageable);

        assertNotNull(result);
        assertEquals(0, result.totalElements());
        assertTrue(result.content().isEmpty());
        verify(userNotificationRepository).findAllByUserId(userId, pageable);
    }

    @Test
    @DisplayName("Should mark notification as read")
    void markAsReadTestCase1() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UserNotification notification = new UserNotification();
        notification.setId(id);
        notification.setUserId(userId);
        notification.setViewed(false);

        when(userNotificationRepository.findByIdAndUserId(id, userId))
                .thenReturn(Optional.of(notification));
        when(userNotificationRepository.save(any(UserNotification.class)))
                .thenReturn(notification);

        userNotificationService.markAsRead(id, userId);

        assertTrue(notification.getViewed());
        verify(userNotificationRepository).save(notification);
    }

    @Test
    @DisplayName("Should throw UserNotificationNotFoundException when notification not found")
    void markAsReadTestCase2() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(userNotificationRepository.findByIdAndUserId(id, userId))
                .thenReturn(Optional.empty());

        assertThrows(UserNotificationNotFoundException.class, () -> {
            userNotificationService.markAsRead(id, userId);
        });

        verify(userNotificationRepository).findByIdAndUserId(id, userId);
    }

    @Test
    @DisplayName("Should throw UserNotificationHasAlreadyBeenReadException when already read")
    void markAsReadTestCase3() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UserNotification notification = new UserNotification();
        notification.setId(id);
        notification.setUserId(userId);
        notification.setViewed(true);

        when(userNotificationRepository.findByIdAndUserId(id, userId))
                .thenReturn(Optional.of(notification));

        assertThrows(UserNotificationHasAlreadyBeenReadException.class, () -> {
            userNotificationService.markAsRead(id, userId);
        });

        verify(userNotificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should create notification for payment generated")
    void notifyPaymentGeneratedTestCase1() {
        UUID userId = UUID.randomUUID();
        BigDecimal value = new BigDecimal("100.00");

        UserNotificationCategory category = new UserNotificationCategory();
        category.setId(3);
        category.setName("PURCHASE");

        NotifyPaymentGeneratedMessage message = new NotifyPaymentGeneratedMessage(
                UUID.randomUUID(), UUID.randomUUID(), userId, value);

        when(userNotificationCategoryRepository.getReferenceById(3))
                .thenReturn(category);
        when(userNotificationRepository.save(any(UserNotification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userNotificationService.notifyPaymentGenerated(message);

        verify(userNotificationRepository).save(any(UserNotification.class));
    }

    @Test
    @DisplayName("Should create notification for payment confirmed")
    void notifyPaymentConfirmedTestCase1() {
        UUID userId = UUID.randomUUID();
        BigDecimal value = new BigDecimal("100.00");

        UserNotificationCategory category = new UserNotificationCategory();
        category.setId(3);
        category.setName("PURCHASE");

        NotifyPaymentConfirmedMessage message = new NotifyPaymentConfirmedMessage(
                UUID.randomUUID(), UUID.randomUUID(), userId, value);

        when(userNotificationCategoryRepository.getReferenceById(3))
                .thenReturn(category);
        when(userNotificationRepository.save(any(UserNotification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userNotificationService.notifyPaymentConfirmed(message);

        verify(userNotificationRepository).save(any(UserNotification.class));
    }

    @Test
    @DisplayName("Should create notification for cancelled order")
    void notifyCancelledOrderTestCase1() {
        UUID userId = UUID.randomUUID();
        UUID salesOrderId = UUID.randomUUID();
        BigDecimal value = new BigDecimal("100.00");

        UserNotificationCategory category = new UserNotificationCategory();
        category.setId(3);
        category.setName("PURCHASE");

        NotifyOrderCancelledMessage message = new NotifyOrderCancelledMessage(
                salesOrderId,
                userId,
                List.of(new NotifyOrderCancelledMessage.OrderItem(UUID.randomUUID(), 2)),
                value);

        when(userNotificationCategoryRepository.getReferenceById(3))
                .thenReturn(category);
        when(userNotificationRepository.save(any(UserNotification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userNotificationService.notifyCancelledOrder(message);

        verify(userNotificationRepository).save(any(UserNotification.class));
    }
}