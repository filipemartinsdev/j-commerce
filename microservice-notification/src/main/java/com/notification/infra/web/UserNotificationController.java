package com.notification.infra.web;

import com.notification.application.dto.PagedResponse;
import com.notification.application.dto.StandardResponse;
import com.notification.application.dto.UserNotificationResponse;
import com.notification.application.service.UserNotificationService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class UserNotificationController {
    private final UserNotificationService userNotificationService;

    public UserNotificationController(UserNotificationService userNotificationService) {
        this.userNotificationService = userNotificationService;
    }

    @GetMapping
    public ResponseEntity<StandardResponse<PagedResponse<UserNotificationResponse>>> getUserNotifications(@AuthenticationPrincipal Jwt jwt, Pageable pageable) {
        UUID authenticatedUserId = UUID.fromString(jwt.getSubject());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponse.success(
                        userNotificationService.getAll(authenticatedUserId, pageable)
                ));
    }
}
