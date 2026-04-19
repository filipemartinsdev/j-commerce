package com.products.infra.web.catalogue;

import com.products.application.dto.PagedResponse;
import com.products.application.dto.StandardResponse;
import com.products.application.dto.catalogue.CreateShoppingCartItemRequest;
import com.products.application.dto.catalogue.ShoppingCartItemResponse;
import com.products.application.service.ShoppingCartService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shopping-cart")
public class ShoppingCartController {
    private final ShoppingCartService shoppingCartService;

    public ShoppingCartController(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    @GetMapping
    public ResponseEntity<StandardResponse<PagedResponse<ShoppingCartItemResponse>>> getAllItems(
            @AuthenticationPrincipal Jwt jwt,
            Pageable pageable
    ){
        UUID authenticatedUserId = UUID.fromString(jwt.getSubject());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponse.success(
                        shoppingCartService.getAllItems(authenticatedUserId, pageable)
                ));
    }

    @PostMapping
    public ResponseEntity<Void> create (
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateShoppingCartItemRequest request
    ){
        UUID authenticatedUserId = UUID.fromString(jwt.getSubject());
        shoppingCartService.createItemByUserId(request, authenticatedUserId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @DeleteMapping
    public ResponseEntity<StandardResponse<PagedResponse<ShoppingCartItemResponse>>> deleteAllItems(
            @AuthenticationPrincipal Jwt jwt
    ){
        UUID authenticatedUserId = UUID.fromString(jwt.getSubject());
        shoppingCartService.deleteAllItemsByUserId(authenticatedUserId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<StandardResponse<PagedResponse<ShoppingCartItemResponse>>> deleteById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ){
        UUID authenticatedUserId = UUID.fromString(jwt.getSubject());
        shoppingCartService.deleteItemById(id, authenticatedUserId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }
}
