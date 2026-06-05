package com.products.docs;

import com.products.application.dto.catalogue.CreateWishlistItemRequest;
import com.products.application.dto.catalogue.WishlistItemResponse;
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
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

@Tag(name = "Wishlist")
public interface WishlistControllerDocs {
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Get user wishlist"
    )
    @ApiResponses({
            @ApiResponse(
                   responseCode = "200",
                   description = "User wishlist retrieved successfully",
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
    ResponseEntity<StandardResponse<PagedResponse<WishlistItemResponse>>> getWishlist(
            Jwt jwt, Pageable pageable
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Add item to wishlist"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Item added to wishlist successfully",
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
                    description = "Product not found",
                    content = @Content
            )
    })
    ResponseEntity<Void> addItemToWishlist(
            CreateWishlistItemRequest request, Jwt jwt
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Delete item from wishlsit"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Item removed from wishlist successfully",
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
                    description = "Wishlist item not found",
                    content = @Content
            )
    })
    ResponseEntity<Void> deleteItemFromWishlist(
            UUID id, Jwt jwt
    );

    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Clear user wishlist"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Wishlist cleared successfully",
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
    ResponseEntity<Void> deleteAllItems (Jwt jwt);
}
