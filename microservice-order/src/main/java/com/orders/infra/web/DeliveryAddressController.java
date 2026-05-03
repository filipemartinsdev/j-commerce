package com.orders.infra.web;

import com.orders.application.dto.*;
import com.orders.application.service.DeliveryAddressService;
import com.orders.docs.DeliveryAddressControllerDocs;
import com.orders.domain.entity.DeliveryAddress;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/delivery-addresses")
public class DeliveryAddressController implements DeliveryAddressControllerDocs {
    private final DeliveryAddressService deliveryAddressService;

    public DeliveryAddressController(DeliveryAddressService deliveryAddressService) {
        this.deliveryAddressService = deliveryAddressService;
    }

    @GetMapping
    public ResponseEntity<StandardResponse<PagedResponse<DeliveryAddressResponse>>> getAllAddressesByUser(
            @AuthenticationPrincipal Jwt jwt,
            Pageable pageable
    ) {
        UUID authenticatedUserId = UUID.fromString(jwt.getSubject());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponse.success(deliveryAddressService.getAllByUserId(
                        authenticatedUserId, pageable
                )));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StandardResponse<DeliveryAddressResponse>> getAddressById(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID authenticatedUserId = UUID.fromString(jwt.getSubject());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponse.success(
                        deliveryAddressService.getById(id, authenticatedUserId)
                ));
    }

    @PostMapping
    public ResponseEntity<StandardResponse<DeliveryAddressResponse>> createAddress(
            @Valid @RequestBody CreateDeliveryAddressRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID authenticatedUserId = UUID.fromString(jwt.getSubject());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(StandardResponse.success(
                        deliveryAddressService.createByUserId(request, authenticatedUserId)
                ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<StandardResponse<DeliveryAddressResponse>> deleteAddress(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID authenticatedUserId = UUID.fromString(jwt.getSubject());
        deliveryAddressService.deleteById(id, authenticatedUserId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();

    }

    @PatchMapping("/{id}")
    public ResponseEntity<StandardResponse<DeliveryAddressResponse>> updateAddress(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDeliveryAddressRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID authenticatedUserId = UUID.fromString(jwt.getSubject());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(StandardResponse.success(
                        deliveryAddressService.updateById(id, authenticatedUserId, request)
                ));
    }
}
