package com.products.docs;

import com.products.application.dto.PagedResponse;
import com.products.application.dto.StandardResponse;
import com.products.application.dto.admin.CreateStockEntryRequest;
import com.products.application.dto.admin.ProductStockResponse;
import com.products.application.dto.admin.StockMovementResponse;
import com.products.application.dto.admin.StockMovementTypeResponse;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@Tag(name = "Admin stock management")
public interface AdminStockControllerDocs {

    @Operation(
            summary = "Get all stock entries"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Stock entries retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            )
    })
    ResponseEntity<StandardResponse<PagedResponse<ProductStockResponse>>> getAllStock(
            @RequestParam(required = false) UUID productId,
            Pageable pageable
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Create new stock entry"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Stock entry created successfully",
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
                    description = "Product SKU not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Product is out of stock",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            )
    })
    ResponseEntity<Void> createStockEntry(
            @Valid @RequestBody CreateStockEntryRequest request,
            @AuthenticationPrincipal Jwt jwt
    );

    @Operation(
            summary = "Get all stock movements"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Stock movements retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            )
    })
    ResponseEntity<StandardResponse<PagedResponse<StockMovementResponse>>> getAllStockMovements(
            @RequestParam(required = false) UUID productSKUId,
            Pageable pageable
    );

    @Operation(
            summary = "Get all stock movement types"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Stock movement types retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            )
    })
    ResponseEntity<StandardResponse<List<StockMovementTypeResponse>>> getAllStockMovementsTypes();
}