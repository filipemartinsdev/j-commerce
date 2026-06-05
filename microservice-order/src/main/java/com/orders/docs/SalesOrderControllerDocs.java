package com.orders.docs;

import com.orders.application.dto.SalesOrderResponse;
import com.orders.application.dto.SalesOrderSummaryResponse;
import io.github.responsekit.core.PagedResponse;
import io.github.responsekit.core.StandardResponse;
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

@Tag(name = "Sales order")
public interface SalesOrderControllerDocs {

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get all sales orders for authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Sales orders retrieved successfully",
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
    ResponseEntity<StandardResponse<PagedResponse<SalesOrderResponse>>> getAllSalesOrders(
            @AuthenticationPrincipal Jwt jwt,
            Pageable pageable
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get sales order by ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Sales order retrieved successfully",
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
                    description = "Sales order not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied to this sales order",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            )
    })
    ResponseEntity<StandardResponse<SalesOrderSummaryResponse>> getSalesOrderById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Request to cancel a sales order"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Sales order cancellation requested successfully",
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
                    description = "Sales order not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Cannot cancel this sales order",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            )
    })
    ResponseEntity<Void> requestToCancelSalesOrder(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    );
}