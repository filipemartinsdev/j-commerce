package com.products.infra.web.catalogue;

import com.products.application.dto.catalogue.CreateWishlistItemRequest;
import com.products.application.dto.catalogue.WishlistItemResponse;
import com.products.application.service.WishlistService;
import com.products.docs.WishlistControllerDocs;
import io.github.responsekit.core.PagedResponse;
import io.github.responsekit.core.StandardResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wishlist")
public class WishlistController implements WishlistControllerDocs {
    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public ResponseEntity<StandardResponse<PagedResponse<WishlistItemResponse>>> getWishlist(
            @AuthenticationPrincipal Jwt jwt,
            Pageable pageable
    ){
        UUID authenticatedUserId = UUID.fromString(jwt.getSubject());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponse.success(
                        wishlistService.getAllItems(authenticatedUserId, pageable)
                ).build());
    }


    @PostMapping
    public ResponseEntity<Void> addItemToWishlist(
            @Valid @RequestBody CreateWishlistItemRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID authenticatedUserId = UUID.fromString(jwt.getSubject());

        wishlistService.createItem(request, authenticatedUserId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItemFromWishlist(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID authenticatedUserId = UUID.fromString(jwt.getSubject());
        wishlistService.deleteItem(id, authenticatedUserId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllItems (
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID authenticatedUserId = UUID.fromString(jwt.getSubject());
        wishlistService.deleteAllItemsByUserId(authenticatedUserId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }
}
