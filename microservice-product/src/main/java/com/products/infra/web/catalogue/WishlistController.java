package com.products.infra.web.catalogue;

import com.products.application.dto.Response;
import com.products.application.dto.catalogue.WishlistItemResponse;
import com.products.application.service.ScrollSubrangeExtractor;
import com.products.application.service.WishlistService;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.query.ScrollSubrange;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
public class WishlistController {
    private final WishlistService wishlistService;
    private final ScrollSubrangeExtractor scrollSubrangeExtractor;

    public WishlistController(WishlistService wishlistService, ScrollSubrangeExtractor scrollSubrangeExtractor) {
        this.wishlistService = wishlistService;
        this.scrollSubrangeExtractor = scrollSubrangeExtractor;
    }


    @QueryMapping
    @PreAuthorize("hasRole('USER')")
    public Window<WishlistItemResponse> wishlist(ScrollSubrange subrange, @AuthenticationPrincipal Jwt jwt){
        ScrollPosition position = scrollSubrangeExtractor.getPosition(subrange);
        Limit limit = scrollSubrangeExtractor.getLimit(subrange);
        UUID userId = UUID.fromString(jwt.getSubject());

        return wishlistService.get(userId, position, limit);
    }

    @MutationMapping
    @PreAuthorize("hasRole('USER')")
    public Response addWishlistItem(@Argument String productId, @AuthenticationPrincipal Jwt jwt){
        UUID userId = UUID.fromString(jwt.getSubject());

        wishlistService.add(userId, productId);
        return new Response(true, "Item added successfully");
    }

    @MutationMapping
    @PreAuthorize("hasRole('USER')")
    public Response removeWishlistItem(@Argument String productId, @AuthenticationPrincipal Jwt jwt){
        UUID userId = UUID.fromString(jwt.getSubject());

        wishlistService.remove(userId, productId);
        return new Response(true, "Item removed successfully");
    }

    @MutationMapping
    @PreAuthorize("hasRole('USER')")
    public Response clearWishlist(@AuthenticationPrincipal Jwt jwt){
        UUID userId = UUID.fromString(jwt.getSubject());

        wishlistService.clear(userId);
        return new Response(true, "Wishlist cleared successfully");
    }
}
