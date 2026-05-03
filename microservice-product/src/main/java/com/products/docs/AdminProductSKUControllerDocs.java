package com.products.docs;

import com.products.application.dto.PagedResponse;
import com.products.application.dto.StandardResponse;
import com.products.application.dto.admin.CreateProductSKURequest;
import com.products.application.dto.admin.ProductSKUAdminResponse;
import com.products.application.dto.admin.UpdateProductSKURequest;
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

import java.util.UUID;

@Tag(name = "Admin SKU management")
public interface AdminProductSKUControllerDocs {

    @Operation(
            summary = "Get all product SKUs"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product SKUs retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            )
    })
    ResponseEntity<StandardResponse<PagedResponse<ProductSKUAdminResponse>>> getAllProductSKUs(
            @RequestParam(required = false, name = "productId") UUID productId,
            Pageable pageable
    );

    @Operation(
            summary = "Get product SKU by ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product SKU retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product SKU not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            )
    })
    ResponseEntity<StandardResponse<ProductSKUAdminResponse>> getProductSKUById(
            @PathVariable UUID skuId
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Create new product SKU"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Product SKU created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "SKU already in use",
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
                    description = "Product not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Product is not active",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            )
    })
    ResponseEntity<StandardResponse<ProductSKUAdminResponse>> createProductSKU(
            @Valid @RequestBody CreateProductSKURequest request,
            @AuthenticationPrincipal Jwt jwt
    );

    @Operation(
            summary = "Update existing product SKU"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product SKU updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "SKU already in use",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
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
                    description = "Product is not active",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            )
    })
    ResponseEntity<StandardResponse<ProductSKUAdminResponse>> updateProductSKU(
            @PathVariable UUID skuId,
            @Valid @RequestBody UpdateProductSKURequest request
    );

    @Operation(
            summary = "Delete product SKU by ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product SKU deleted successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product SKU not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            )
    })
    ResponseEntity<Void> deleteProductSKUById(@PathVariable UUID productSKUId);
}