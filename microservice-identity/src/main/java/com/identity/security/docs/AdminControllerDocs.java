package com.identity.security.docs;

import com.identity.common.dto.PagedResponse;
import com.identity.common.dto.StandardResponse;
import com.identity.security.application.dto.UpdateUserRole;
import com.identity.security.application.dto.UserCredentialsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@Tag(name = "Admin management")
public interface AdminControllerDocs {
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Update user role"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Client not authenticated",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Authenticated user can't perform this operation",
                    content = @Content
            )
    })
    ResponseEntity<Void> updateUserRole(UUID userId,UpdateUserRole request);

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Retrieve all users"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "All users retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Client not authenticated",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Authenticated user can't perform this operation",
                    content = @Content
            )
    })
    ResponseEntity<StandardResponse<PagedResponse<UserCredentialsResponse>>> getAllUsers(Pageable pageable);

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get user by ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User retrieved by ID successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found by ID",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Client not authenticated",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Authenticated user can't perform this operation",
                    content = @Content
            )
    })
    ResponseEntity<StandardResponse<UserCredentialsResponse>> getUserById(UUID userId);
}