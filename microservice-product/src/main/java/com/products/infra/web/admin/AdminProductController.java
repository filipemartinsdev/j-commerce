package com.products.infra.web.admin;

import com.products.application.dto.PagedResponse;
import com.products.application.dto.StandardResponse;
import com.products.application.dto.admin.*;
import com.products.application.service.ProductManagementService;
import com.products.docs.AdminProductControllerDocs;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/api/v1")
public class AdminProductController implements AdminProductControllerDocs {
    private final ProductManagementService adminProductService;

    public AdminProductController(ProductManagementService adminProductService) {
        this.adminProductService = adminProductService;
    }

    @GetMapping("/products")
    public ResponseEntity<StandardResponse<PagedResponse<ProductAdminResponse>>> getAllProducts(
            @RequestParam(required = false, defaultValue = "-1") Integer categoryId,
            Pageable pageable
    ) {
        PagedResponse<ProductAdminResponse> pagedResponse;

        if (categoryId == -1)
            pagedResponse = adminProductService.getAllProducts(pageable);
        else
            pagedResponse = adminProductService.getAllProductsByCategoryId(categoryId, pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponse.success(pagedResponse));
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
}
