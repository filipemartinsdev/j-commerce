package com.orders.docs;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import com.orders.application.dto.ShippingResponse;

import io.github.responsekit.core.PagedResponse;
import io.github.responsekit.core.StandardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Admin Shipping Management")
public interface AdminShippingControllerDocs {

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get all shippings")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Shippings retrieved successfully",
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
                    responseCode = "403",
                    description = "Authenticated user can't perform this operation",
                    content = @Content
            )
    })
    ResponseEntity<StandardResponse<PagedResponse<ShippingResponse>>> getAll(
            UUID salesOrderId,
            Pageable pageable
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get shipping by ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Shipping retrieved successfully",
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
                    responseCode = "403",
                    description = "Authenticated user can't perform this operation",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Shipping not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            )
    })
    ResponseEntity<StandardResponse<ShippingResponse>> getById(
            UUID id
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Dispatch a shipping")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Shipping dispatched successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Shipping was already been dispatched or order hasn't been confirmed yet",
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
                    responseCode = "403",
                    description = "Authenticated user can't perform this operation",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Shipping not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            )
    })
    ResponseEntity<StandardResponse<Void>> dispatchShipping(
            UUID id
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Check in a shipping (start transit)")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Shipping check-in successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Shipping is already in transit, cancelled, delivered, or wasn't dispatched yet",
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
                    responseCode = "403",
                    description = "Authenticated user can't perform this operation",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Shipping not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            )
    })
    ResponseEntity<StandardResponse<Void>> checkInShipping(
            UUID id,
            Jwt jwt
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Check out a shipping (finish transit)")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Shipping check-out successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Shipping is not in transit",
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
                    responseCode = "403",
                    description = "Authenticated user can't perform this operation",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Shipping not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            )
    })
    ResponseEntity<StandardResponse<Void>> checkOutShipping(
            UUID id
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Cancel a shipping")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Shipping cancelled successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Shipping is cancelled or delivered",
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
                    responseCode = "403",
                    description = "Authenticated user can't perform this operation",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Shipping not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            )
    })
    ResponseEntity<StandardResponse<Void>> cancelShipping(
            UUID id
    );
}
