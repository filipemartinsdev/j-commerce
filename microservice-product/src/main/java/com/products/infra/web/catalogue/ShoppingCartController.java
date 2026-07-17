package com.products.infra.web.catalogue;

import com.products.application.dto.catalogue.ConfirmShoppingCartRequest;
import com.products.application.dto.catalogue.CreateShoppingCartItemRequest;
import com.products.application.dto.catalogue.ShoppingCartResponse;
import com.products.application.service.ShoppingCartService;
import com.products.docs.ShoppingCartControllerDocs;
import io.github.responsekit.core.StandardResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shopping-cart")
public class ShoppingCartController implements ShoppingCartControllerDocs {
    private final ShoppingCartService shoppingCartService;

    public ShoppingCartController(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    @GetMapping
    public ResponseEntity<StandardResponse<ShoppingCartResponse>> getAllItems(
            @AuthenticationPrincipal Jwt jwt
    ){
        UUID authenticatedUserId = UUID.fromString(jwt.getSubject());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponse.success(
                        shoppingCartService.getAllItems(authenticatedUserId)
                ).build());
    }

    @PostMapping
    public ResponseEntity<StandardResponse<Void>> create (
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateShoppingCartItemRequest request
    ){
        UUID authenticatedUserId = UUID.fromString(jwt.getSubject());
        shoppingCartService.createItemByUserId(request, authenticatedUserId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(StandardResponse.success().build());
    }

    @DeleteMapping
    public ResponseEntity<StandardResponse<Void>> deleteAllItems(
            @AuthenticationPrincipal Jwt jwt
    ){
        UUID authenticatedUserId = UUID.fromString(jwt.getSubject());
        shoppingCartService.deleteAllItemsByUserId(authenticatedUserId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponse.success().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<StandardResponse<Void>> deleteById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ){
        UUID authenticatedUserId = UUID.fromString(jwt.getSubject());
        shoppingCartService.deleteItemByProductSKUId(id, authenticatedUserId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponse.success().build());
    }

    @PostMapping("/checkout")
    public ResponseEntity<StandardResponse<Void>> confirm(
            @Valid @RequestBody ConfirmShoppingCartRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID authenticatedUserId = UUID.fromString(jwt.getSubject());
        shoppingCartService.confirmShoppingCart(request, authenticatedUserId, "Bearer "+jwt.getTokenValue());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(StandardResponse.success().build());
    }
}
