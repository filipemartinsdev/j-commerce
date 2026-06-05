package com.orders.infra.web;

import com.orders.application.dto.ShippingResponse;
import com.orders.application.service.AdminShippingService;
import io.github.responsekit.core.PagedResponse;
import io.github.responsekit.core.StandardResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// TODO: openAPI docs
@RestController
@RequestMapping("/admin/api/v1/shippings")
public class AdminShippingController {
    private final AdminShippingService adminShippingService;

    public AdminShippingController(AdminShippingService adminShippingService) {
        this.adminShippingService = adminShippingService;
    }

    @GetMapping
    public ResponseEntity<StandardResponse<PagedResponse<ShippingResponse>>> getAll(
            @RequestParam(required = false) UUID salesOrderId,
            Pageable pageable
    ){
        PagedResponse<ShippingResponse> response;

        if (salesOrderId == null)
            response = adminShippingService.getAll(pageable);
        else
            response = adminShippingService.getAllBySalesOrderId(salesOrderId, pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponse.success(response).build());
    }

    @PostMapping("/{id}")
    public ResponseEntity<StandardResponse<Void>> dispatchShipping(
            @PathVariable UUID id
    ){
        adminShippingService.dispatchShipping(id);

        return ResponseEntity.ok(
                StandardResponse.success().build()
        );
    }

    @PostMapping("/{id}/check-in")
    public ResponseEntity<StandardResponse<Void>> checkInShipping(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ){
        UUID authenticatedUserId = UUID.fromString(jwt.getSubject());

        adminShippingService.startShipping(id, authenticatedUserId);

        return ResponseEntity.ok(
                StandardResponse.success().build()
        );
    }

    @PostMapping("/{id}/check-out")
    public ResponseEntity<StandardResponse<Void>> checkOutShipping(
            @PathVariable UUID id
    ){
        adminShippingService.finishShipping(id);

        return ResponseEntity.ok(
                StandardResponse.success().build()
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<StandardResponse<Void>> cancelShipping(
            @PathVariable UUID id
    ){
        adminShippingService.cancelShipping(id);

        return ResponseEntity.ok(
                StandardResponse.success().build()
        );
    }
}
