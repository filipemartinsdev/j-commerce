package com.products.infra.web.admin;

import com.products.application.dto.admin.CreateStockMovementRequest;
import com.products.application.service.ScrollSubrangeExtractor;
import com.products.application.service.StockService;
import com.products.domain.entity.StockMovement;
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
public class StockController {
    private final StockService stockService;
    private final ScrollSubrangeExtractor scrollSubrangeExtractor;

    public StockController(StockService stockService, ScrollSubrangeExtractor scrollSubrangeExtractor) {
        this.stockService = stockService;
        this.scrollSubrangeExtractor = scrollSubrangeExtractor;
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('STOCK_MANAGER', 'LOGISTICS', 'ADMIN')")
    public Window<StockMovement> stockMovements(
            @Argument String SKU,
            @Argument Integer typeId,
            ScrollSubrange scrollSubrange
    ){
        ScrollPosition position = scrollSubrangeExtractor.getPosition(scrollSubrange);
        Limit limit = scrollSubrangeExtractor.getLimit(scrollSubrange);

        return resolveStockMovementsRequest(SKU, typeId, position, limit);
    }

    private Window<StockMovement> resolveStockMovementsRequest(
            String SKU,
            Integer typeId,
            ScrollPosition position,
            Limit limit
    ) {
        if (SKU != null && typeId != null)
            return stockService.getAllMovements(SKU, typeId, position, limit);

        else if (SKU != null)
            return stockService.getAllMovements(SKU, position, limit);

        else if (typeId != null)
            return stockService.getAllMovements(typeId, position, limit);

        else
            return stockService.getAllMovements(position, limit);
    }

    @MutationMapping
    @PreAuthorize("hasAnyRole('STOCK_MANAGER', 'LOGISTICS', 'ADMIN')")
    public StockMovement createStockMovement(
            @Argument CreateStockMovementRequest request,
            @AuthenticationPrincipal Jwt jwt
    ){
        var userId = UUID.fromString(jwt.getSubject());
        return stockService.createMovement(request, userId);
    }
}
