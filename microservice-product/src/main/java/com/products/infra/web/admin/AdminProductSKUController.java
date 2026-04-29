package com.products.infra.web.admin;

import com.products.application.dto.PagedResponse;
import com.products.application.dto.StandardResponse;
import com.products.application.dto.admin.CreateProductSKURequest;
import com.products.application.dto.admin.ProductSKUAdminResponse;
import com.products.application.dto.admin.UpdateProductSKURequest;
import com.products.application.service.ProductSKUManagementService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/api/v1")
public class AdminProductSKUController {
    private final ProductSKUManagementService adminProductSKUService;

    public AdminProductSKUController(ProductSKUManagementService adminProductSKUService) {
        this.adminProductSKUService = adminProductSKUService;
    }

    @GetMapping("/skus")
    public ResponseEntity<StandardResponse<PagedResponse<ProductSKUAdminResponse>>> getAllProductSKUs (
            @RequestParam(required = false, name = "productId") UUID productId,
            Pageable pageable
    ) {
        PagedResponse<ProductSKUAdminResponse> response;

        if (productId == null)
            response = adminProductSKUService.getAllProductSKUs(pageable);
        else
            response = adminProductSKUService.getAllProductSKUsByProductId(productId, pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponse.success(response));
    }

    @GetMapping("/skus/{skuId}")
    public ResponseEntity<StandardResponse<ProductSKUAdminResponse>> getProductSKUById(
            @PathVariable UUID skuId
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponse.success(
                        adminProductSKUService.getProductSKUById(skuId)
                ));
    }

    @PostMapping("/skus")
    public ResponseEntity<StandardResponse<ProductSKUAdminResponse>> createProductSKU(
            @Valid @RequestBody CreateProductSKURequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID authenticatedUserId = UUID.fromString(jwt.getSubject());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(StandardResponse.success(
                        adminProductSKUService.createProductSKU(request, authenticatedUserId)
                ));
    }

    @PatchMapping("/skus/{skuId}")
    public ResponseEntity<StandardResponse<ProductSKUAdminResponse>> updateProductSKU(
            @PathVariable UUID skuId,
            @Valid @RequestBody UpdateProductSKURequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponse.success(
                        adminProductSKUService.updateProductSKU(skuId, request)
                ));
    }

    @DeleteMapping("/skus/{productSKUId}")
    public ResponseEntity<Void> deleteProductSKUById(@PathVariable UUID productSKUId) {
        adminProductSKUService.deleteProductSKUById(productSKUId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }
}
