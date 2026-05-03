package com.orders.docs;

import com.orders.application.dto.CreateDeliveryAddressRequest;
import com.orders.application.dto.DeliveryAddressResponse;
import com.orders.application.dto.PagedResponse;
import com.orders.application.dto.StandardResponse;
import com.orders.application.dto.UpdateDeliveryAddressRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@Tag(name = "Delivery address")
public interface DeliveryAddressControllerDocs {

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get all delivery addresses for authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Delivery addresses retrieved successfully",
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
    ResponseEntity<StandardResponse<PagedResponse<DeliveryAddressResponse>>> getAllAddressesByUser(
            @AuthenticationPrincipal Jwt jwt,
            Pageable pageable
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get delivery address by ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Delivery address retrieved successfully",
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
                    description = "Delivery address not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied to this delivery address",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            )
    })
    ResponseEntity<StandardResponse<DeliveryAddressResponse>> getAddressById(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Create a new delivery address"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Delivery address created successfully",
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
    ResponseEntity<StandardResponse<DeliveryAddressResponse>> createAddress(
            @Valid @RequestBody CreateDeliveryAddressRequest request,
            @AuthenticationPrincipal Jwt jwt
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Delete a delivery address"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Delivery address deleted successfully",
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
                    description = "Delivery address not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied to this delivery address",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            )
    })
    ResponseEntity<StandardResponse<DeliveryAddressResponse>> deleteAddress(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Update a delivery address"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Delivery address updated successfully",
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
                    description = "Delivery address not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied to this delivery address",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            )
    })
    ResponseEntity<StandardResponse<DeliveryAddressResponse>> updateAddress(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDeliveryAddressRequest request,
            @AuthenticationPrincipal Jwt jwt
    );
}