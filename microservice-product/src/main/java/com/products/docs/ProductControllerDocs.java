package com.products.docs;

import com.products.application.dto.ProductCategoryResponse;
import com.products.application.dto.catalogue.ProductCatalogueResponse;
import com.products.application.dto.catalogue.ProductSummaryCatalogueResponse;
import io.github.responsekit.core.SlicedResponse;
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

import java.util.UUID;

@Tag(name = "Catalogue")
public interface ProductControllerDocs {
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get product categories"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product categories retrieved successfully",
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
            )
    })
    ResponseEntity<StandardResponse<SlicedResponse<ProductCategoryResponse>>> getCategories(String opaqueCursor, Pageable pageable);

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get products"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Products retrieved successfully",
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
            )
    })
    ResponseEntity<StandardResponse<SlicedResponse<ProductSummaryCatalogueResponse>>> getProducts(
            String query, Integer categoryId, String opaqueCursor, Pageable pageable
    );


    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get product by ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product categories retrieved successfully",
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
                    responseCode = "404",
                    description = "Product not found by ID",
                    content = @Content
            )
    })
    ResponseEntity<StandardResponse<ProductCatalogueResponse>> getProductById(UUID productId);
}
