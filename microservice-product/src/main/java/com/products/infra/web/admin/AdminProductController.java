package com.products.infra.web.admin;

import com.products.application.dto.PagedResponse;
import com.products.application.dto.StandardResponse;
import com.products.application.dto.admin.*;
import com.products.application.service.AdminProductService;
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
public class AdminProductController {
    private final AdminProductService adminProductService;

    public AdminProductController(AdminProductService adminProductService) {
        this.adminProductService = adminProductService;
    }

    @GetMapping("/products")
    public ResponseEntity<StandardResponse<PagedResponse<ProductAdminResponse>>> getAllProducts(Pageable pageable) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponse.success(
                        adminProductService.getAllProducts(pageable)
                ));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<StandardResponse<ProductAdminResponse>> getProductById(@PathVariable UUID productId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponse.success(
                        adminProductService.getProductById(productId)
                ));
    }

    @PostMapping("/products")
    public ResponseEntity<StandardResponse<ProductAdminResponse>> createProduct(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(StandardResponse.success(
                        adminProductService.createProduct(request)
                ));
    }

    @PatchMapping("/products/{productId}")
    public ResponseEntity<StandardResponse<ProductAdminResponse>> updateProduct(
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponse.success(
                        adminProductService.updateProduct(productId, request)
                ));
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<Void> deleteProductById(@PathVariable UUID productId) {
        adminProductService.deleteProductById(productId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }


    @GetMapping("/skus")
    public ResponseEntity<StandardResponse<PagedResponse<ProductSKUAdminResponse>>> getAllProductSKUs (
            @RequestParam UUID productId,
            Pageable pageable
    ) {
        PagedResponse<ProductSKUAdminResponse> response;

        if (productId == null)
            response = adminProductService.getAllProductSKUs(pageable);
        else
            response = adminProductService.getAllProductSKUsByProductId(productId, pageable);

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
                        adminProductService.getProductSKUById(skuId)
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
                        adminProductService.createProductSKU(request, authenticatedUserId)
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
                        adminProductService.updateProductSKU(skuId, request)
                ));
    }

    @DeleteMapping("/skus/{productSKUId}")
    public ResponseEntity<Void> deleteProductSKUById(@PathVariable UUID productSKUId) {
        adminProductService.deleteProductSKUById(productSKUId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }
}
