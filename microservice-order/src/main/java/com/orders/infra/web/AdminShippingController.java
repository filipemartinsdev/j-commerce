package com.orders.infra.web;

import com.orders.application.dto.ShippingResponse;
import com.orders.application.service.AdminShippingService;
import com.orders.docs.AdminShippingControllerDocs;

import io.github.responsekit.core.PagedResponse;
import io.github.responsekit.core.StandardResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/api/v1/shippings")
public class AdminShippingController implements AdminShippingControllerDocs{
    private final AdminShippingService adminShippingService;

    public AdminShippingController(AdminShippingService adminShippingService) {
        this.adminShippingService = adminShippingService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('LOGISTICS', 'ADMIN')")
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

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('LOGISTICS', 'ADMIN')")
    public ResponseEntity<StandardResponse<ShippingResponse>> getById(
            @PathVariable UUID id
    ){
        return ResponseEntity.ok(
                StandardResponse
                        .success(adminShippingService.getById(id))
                        .build()
        );
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyRole('LOGISTICS', 'ADMIN')")
    public ResponseEntity<StandardResponse<Void>> dispatchShipping(
            @PathVariable UUID id
    ){
        adminShippingService.dispatchShipping(id);

        return ResponseEntity.ok(
                StandardResponse.success().build()
        );
    }

    @PostMapping("/{id}/check-in")
    @PreAuthorize("hasAnyRole('LOGISTICS', 'ADMIN', 'DRIVER')")
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
    @PreAuthorize("hasAnyRole('LOGISTICS', 'DRIVER', 'ADMIN')")
    public ResponseEntity<StandardResponse<Void>> checkOutShipping(
            @PathVariable UUID id
    ){
        adminShippingService.finishShipping(id);

        return ResponseEntity.ok(
                StandardResponse.success().build()
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('LOGISTICS', 'ADMIN')")
    public ResponseEntity<StandardResponse<Void>> cancelShipping(
            @PathVariable UUID id
    ){
        adminShippingService.cancelShipping(id);

        return ResponseEntity.ok(
                StandardResponse.success().build()
        );
    }
}
