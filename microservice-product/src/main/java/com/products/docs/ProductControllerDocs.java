package com.products.docs;

import com.products.application.dto.PagedResponse;
import com.products.application.dto.ProductCategoryResponse;
import com.products.application.dto.StandardResponse;
import com.products.application.dto.catalogue.ProductCatalogueResponse;
import com.products.application.dto.catalogue.ProductSummaryCatalogueResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

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
    ResponseEntity<StandardResponse<PagedResponse<ProductCategoryResponse>>> getCategories(Pageable pageable);

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
    ResponseEntity<StandardResponse<PagedResponse<ProductSummaryCatalogueResponse>>> getProducts(
            Integer category, Pageable pageable
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
