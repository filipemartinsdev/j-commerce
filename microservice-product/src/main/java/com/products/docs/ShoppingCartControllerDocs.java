package com.products.docs;

import com.products.application.dto.PagedResponse;
import com.products.application.dto.StandardResponse;
import com.products.application.dto.catalogue.ConfirmShoppingCartRequest;
import com.products.application.dto.catalogue.CreateShoppingCartItemRequest;
import com.products.application.dto.catalogue.ShoppingCartItemResponse;
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

import java.util.UUID;

@Tag(name = "Shopping cart")
public interface ShoppingCartControllerDocs {

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get all items in the authenticated user shopping cart"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Shopping cart items retrieved successfully",
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
    ResponseEntity<StandardResponse<PagedResponse<ShoppingCartItemResponse>>> getAllItems(
            @AuthenticationPrincipal Jwt jwt,
            Pageable pageable
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Add a product SKU to the authenticated user shopping cart"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Item added to shopping cart successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Item already exists in shopping cart",
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
                    description = "Product SKU not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Product is out of stock",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            )
    })
    ResponseEntity<Void> create(
            @AuthenticationPrincipal Jwt jwt,
            CreateShoppingCartItemRequest request
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Remove all items from the authenticated user shopping cart"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "All items removed from shopping cart successfully",
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
    ResponseEntity<StandardResponse<PagedResponse<ShoppingCartItemResponse>>> deleteAllItems(
            @AuthenticationPrincipal Jwt jwt
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Remove a specific item from the authenticated user shopping cart"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Item removed from shopping cart successfully",
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
                    description = "Shopping cart item not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            )
    })
    ResponseEntity<StandardResponse<PagedResponse<ShoppingCartItemResponse>>> deleteById(
            @AuthenticationPrincipal Jwt jwt,
            UUID id
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Confirm the shopping cart and initiate order creation"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Shopping cart confirmed and order creation initiated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Shopping cart is empty",
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
                    description = "Delivery address not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = StandardResponse.class
                            )
                    )
            )
    })
    ResponseEntity<Void> confirm(
            ConfirmShoppingCartRequest request,
            @AuthenticationPrincipal Jwt jwt
    );
}
