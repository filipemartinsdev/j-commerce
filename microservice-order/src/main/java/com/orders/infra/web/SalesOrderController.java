package com.orders.infra.web;

import com.orders.application.dto.*;
import com.orders.application.service.SalesOrderService;
import com.orders.docs.SalesOrderControllerDocs;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sales-orders")
public class SalesOrderController implements SalesOrderControllerDocs {
    private final SalesOrderService salesOrderService;

    public SalesOrderController(SalesOrderService salesOrderService) {
        this.salesOrderService = salesOrderService;
    }

    @GetMapping
    public ResponseEntity<StandardResponse<PagedResponse<SalesOrderResponse>>> getAllSalesOrders(
            @AuthenticationPrincipal Jwt jwt,
            Pageable pageable
    ) {
        UUID authenticatedUserId = UUID.fromString(jwt.getSubject());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponse.success(salesOrderService.getAllByUserId(authenticatedUserId, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StandardResponse<SalesOrderSummaryResponse>> getSalesOrderById(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        UUID authenticatedUserId = UUID.fromString(jwt.getSubject());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponse.success(salesOrderService.getSummaryById(id, authenticatedUserId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> requestToCancelSalesOrder(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID id
    ) {
        UUID authenticatedUserId = UUID.fromString(jwt.getSubject());
        salesOrderService.requestToCancelOrder(id, authenticatedUserId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }
}
