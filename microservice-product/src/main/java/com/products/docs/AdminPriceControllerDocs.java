package com.products.docs;

import com.products.application.dto.PagedResponse;
import com.products.application.dto.StandardResponse;
import com.products.application.dto.admin.CreateProductSKUPrice;
import com.products.application.dto.admin.ProductSKUPriceResponse;
import com.products.application.dto.admin.UpdateProductSKUPriceRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Tag(name = "Admin price management")
public interface AdminPriceControllerDocs {

    @Operation(
            summary = "Get all product prices"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product prices retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            )
    })
    ResponseEntity<StandardResponse<PagedResponse<ProductSKUPriceResponse>>> getAllPrices(
            @RequestParam(required = false) UUID productSKUId,
            Pageable pageable
    );

    @Operation(
            summary = "Create new product price"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product price created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid price type",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Product SKU without base price",
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
    ResponseEntity<StandardResponse<ProductSKUPriceResponse>> createProductSKUPrice(
            @Valid @RequestBody CreateProductSKUPrice request
    );

    @Operation(
            summary = "Update existing product price"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product price updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid price type",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product price not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            )
    })
    ResponseEntity<StandardResponse<ProductSKUPriceResponse>> updatePriceById(
            @PathVariable UUID priceId,
            @Valid @RequestBody UpdateProductSKUPriceRequest request
    );

    @Operation(
            summary = "Delete product price by ID"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Product price deleted successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Product price not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StandardResponse.class)
                    )
            )
    })
    ResponseEntity<Void> deleteById(@PathVariable UUID priceId);
}