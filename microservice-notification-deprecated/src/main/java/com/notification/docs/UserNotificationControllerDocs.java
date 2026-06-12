package com.notification.docs;

import com.notification.application.dto.PagedResponse;
import com.notification.application.dto.StandardResponse;
import com.notification.application.dto.UserNotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@Tag(name = "User notification")
public interface UserNotificationControllerDocs {

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get all notifications for authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Notifications retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Client not authenticated",
                    content = @Content
            )
    })
    ResponseEntity<StandardResponse<PagedResponse<UserNotificationResponse>>> getUserNotifications(
            @AuthenticationPrincipal Jwt jwt,
            Pageable pageable
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Mark a notification as read"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Notification marked as read successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Client not authenticated",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Notification not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Notification has already been read",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            )
    })
    ResponseEntity<Void> markNotificationAsRead(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    );
}