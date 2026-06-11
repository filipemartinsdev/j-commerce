package com.notification.application.service;

import com.notification.application.dto.UserNotificationResponse;
import com.notification.application.exception.UserNotificationHasAlreadyBeenViewedException;
import com.notification.application.exception.UserNotificationNotFoundException;
import com.notification.application.service.mapper.UserNotificationMapper;
import com.notification.domain.entity.UserNotification;
import com.notification.domain.entity.UserNotificationCategory;
import com.notification.infra.persistence.UserNotificationRepository;
import io.github.responsekit.core.PagedResponse;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.ForbiddenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class UserNotificationServiceTests {
    @Mock private UserNotificationRepository userNotificationRepository;
    @Mock private UserNotificationMapper userNotificationMapper;
    @Mock private EntityManager entityManager;

    @InjectMocks private UserNotificationService userNotificationService;

    @Test @DisplayName("Should retrieve user notifications successfully")
    void getAllByUserIdTestCase1() {
        UUID userId = UUID.randomUUID();
        int page = 0;
        int size = 20;

        UserNotification notification = new UserNotification();
        notification.id = userId;

        UserNotificationResponse notificationResponse = new UserNotificationResponse(userId, "", "", "", false, Instant.now());

        PagedResponse<UserNotificationResponse> expectedResponse = PagedResponse
                .content(List.of(notificationResponse))
                .page(page)
                .size(size)
                .build();

        PanacheQuery<UserNotification> queryMock = Mockito.mock(PanacheQuery.class);

        Mockito.when(userNotificationRepository.findAllByUserId(userId, page, size))
                .thenReturn(queryMock);

        Page pageMock = Page.of(page, size);

        Mockito.when(queryMock.list())
                .thenReturn(List.of(notification));
        Mockito.when(queryMock.page())
                .thenReturn(pageMock);
        Mockito.when(queryMock.hasNextPage())
                .thenReturn(false);
        Mockito.when(queryMock.stream())
                .thenReturn(List.of(notification).stream());
        Mockito.when(userNotificationMapper.toResponse(notification))
                .thenReturn(notificationResponse);

        PagedResponse<UserNotificationResponse> result = userNotificationService.getAllByUserId(userId, page, size);

        assertEquals(expectedResponse.page, result.page);
        assertEquals(expectedResponse.size, result.size);
        assertEquals(expectedResponse.content, result.content);
    }

    @Test @DisplayName("Should retrieve empty PagedResponse if not exists any notification")
    void getAllByUserIdTestCase2() {
        UUID userId = UUID.randomUUID();
        int page = 0;
        int size = 20;

        PagedResponse<UserNotificationResponse> expectedResponse = PagedResponse
                .<UserNotificationResponse>content(List.of())
                .page(page)
                .size(size)
                .build();

        PanacheQuery<UserNotification> queryMock = Mockito.mock(PanacheQuery.class);

        Mockito.when(userNotificationRepository.findAllByUserId(userId, page, size))
                .thenReturn(queryMock);

        Page pageMock = Page.of(page, size);

        Mockito.when(queryMock.list())
                .thenReturn(List.of());
        Mockito.when(queryMock.page())
                .thenReturn(pageMock);

        PagedResponse<UserNotificationResponse> result = userNotificationService.getAllByUserId(userId, page, size);

        assertEquals(expectedResponse.page, result.page);
        assertEquals(expectedResponse.size, result.size);
        assertEquals(expectedResponse.content, result.content);
    }


    @Test @DisplayName("Should mark notification as viewed successfully")
    void viewTestCase1() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UserNotification notification = new UserNotification();
        notification.id = id;
        notification.userId = userId;
        notification.viewed = false;

        Mockito.when(userNotificationRepository.findById(id))
                .thenReturn(notification);

        userNotificationService.view(id, userId);

        Mockito.verify(userNotificationRepository).persist(notification);
    }

    @Test @DisplayName("Should throw UserNotificationHasAlreadyBeenViewedException if notification is viewed")
    void viewTestCase2() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        UserNotification notification = new UserNotification();
        notification.id = id;
        notification.userId = userId;
        notification.viewed = true;

        Mockito.when(userNotificationRepository.findById(id))
                .thenReturn(notification);

        assertThrows(UserNotificationHasAlreadyBeenViewedException.class,
                () -> userNotificationService.view(id, userId));
    }

    @Test @DisplayName("Should throw ForbiddenException if user is not owner of notification")
    void viewTestCase3() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();

        UserNotification notification = new UserNotification();
        notification.id = id;
        notification.userId = otherUserId;
        notification.viewed = false;

        Mockito.when(userNotificationRepository.findById(id))
                .thenReturn(notification);

        assertThrows(ForbiddenException.class,
                () -> userNotificationService.view(id, userId));
    }

    @Test @DisplayName("Should throw UserNotificationNotFoundException if notification not exists by ID")
    void viewTestCase4() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        Mockito.when(userNotificationRepository.findById(id))
                .thenReturn(null);

        assertThrows(UserNotificationNotFoundException.class,
                () -> userNotificationService.view(id, userId));
    }


    @Test @DisplayName("Should create notification successfully")
    void createTestCase1() {
        UUID userId = UUID.randomUUID();
        String title = "Test Title";
        String description = "Test Description";
        Long categoryId = 1L;

        Mockito.when(entityManager.getReference(UserNotificationCategory.class, categoryId))
                .thenReturn(new UserNotificationCategory());

        userNotificationService.create(userId, title, description, categoryId);

        Mockito.verify(userNotificationRepository).persist(Mockito.any(UserNotification.class));
    }
}