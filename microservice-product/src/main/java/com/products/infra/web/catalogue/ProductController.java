package com.products.infra.web.catalogue;

import com.products.application.dto.*;
import com.products.application.dto.catalogue.ProductSummaryCatalogueResponse;
import com.products.application.dto.catalogue.ProductCatalogueResponse;
import com.products.application.service.ProductCatalogueService;
import com.products.application.service.ProductCategoryService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ProductController {
    private final ProductCatalogueService productCatalogueService;
    private final ProductCategoryService productCategoryService;

    public ProductController(ProductCatalogueService productCatalogueService, ProductCategoryService productCategoryService) {
        this.productCatalogueService = productCatalogueService;
        this.productCategoryService = productCategoryService;
    }

    @GetMapping("/categories")
    public ResponseEntity<StandardResponse<PagedResponse<ProductCategoryResponse>>> getCategories(Pageable pageable) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        StandardResponse.success(productCategoryService.getAll(pageable))
                );
    }

    @GetMapping("/products")
    public ResponseEntity<StandardResponse<PagedResponse<ProductSummaryCatalogueResponse>>> getProducts(
            @RequestParam(name = "categoryId", required = false, defaultValue = "-1") Integer category,
            Pageable pageable
    ) {
        PagedResponse<ProductSummaryCatalogueResponse> response;

        if (category == -1)
            response = productCatalogueService.getAll(pageable);
        else
            response = productCatalogueService.getAllByCategoryId(category, pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponse.success(response));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<StandardResponse<ProductCatalogueResponse>> getProductById(@PathVariable UUID productId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponse.success(productCatalogueService.getProductSummaryByProductId(productId)));
    }
}
