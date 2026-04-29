package com.products.infra.web.catalogue;

import com.products.application.dto.*;
import com.products.application.dto.catalogue.ProductSummaryCatalogueResponse;
import com.products.application.dto.catalogue.ProductCatalogueResponse;
import com.products.application.service.ProductCatalogueService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ProductController {
    private final ProductCatalogueService productCatalogueService;
    private final ProductCatalogueService productCatalogueResumeService;

    public ProductController(ProductCatalogueService productCatalogueService, ProductCatalogueService productCatalogueResumeService) {
        this.productCatalogueService = productCatalogueService;
        this.productCatalogueResumeService = productCatalogueResumeService;
    }

    @GetMapping("/categories")
    public ResponseEntity<StandardResponse<PagedResponse<ProductCategoryResponse>>> getCategories(Pageable pageable) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        StandardResponse.success(productCatalogueService.getAllCategories(pageable))
                );
    }

    @GetMapping("/products")
    public ResponseEntity<StandardResponse<PagedResponse<ProductSummaryCatalogueResponse>>> getProducts(
            @RequestParam(name = "category", required = false, defaultValue = "-1") Integer category,
            Pageable pageable
    ) {
        PagedResponse<ProductSummaryCatalogueResponse> response;

        if (category == -1)
            response = productCatalogueResumeService.getAll(pageable);
        else
            response = productCatalogueResumeService.getAllByCategoryId(category, pageable);

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
