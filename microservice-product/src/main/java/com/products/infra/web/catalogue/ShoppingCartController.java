package com.products.infra.web.catalogue;

import com.products.application.dto.Response;
import com.products.application.dto.catalogue.ShoppingCartResponse;
import com.products.application.service.ShoppingCartService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    public ShoppingCartController(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }


    @QueryMapping
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    public ShoppingCartResponse shoppingCart(@AuthenticationPrincipal Jwt jwt){
        UUID userId = UUID.fromString(jwt.getSubject());
        return shoppingCartService.get(userId);
    }

    @MutationMapping
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    public Response addShoppingCartItem(
            @Argument String SKU,
            @Argument Integer units,
            @AuthenticationPrincipal Jwt jwt
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        shoppingCartService.add(userId, SKU, units);

        return new Response(true, "Item added successfully");
    }

    @MutationMapping
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    public Response removeShoppingCartItem(
            @Argument String SKU,
            @AuthenticationPrincipal Jwt jwt
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        shoppingCartService.remove(userId, SKU);

        return new Response(true, "Item removed successfully");
    }

    @MutationMapping
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    public Response clearShoppingCart(
            @AuthenticationPrincipal Jwt jwt
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        shoppingCartService.clear(userId);

        return new Response(true, "Shopping cart cleared successfully");
    }

    @MutationMapping
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    public Response confirmShoppingCart(
            @Argument UUID deliveryAddressId,
            @AuthenticationPrincipal Jwt jwt
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        shoppingCartService.confirm(userId, deliveryAddressId, "Bearer "+jwt.getTokenValue());

        return new Response(true, "Shopping cart confirmed successfully");
    }
}
