package com.products.infra.web.catalogue;

import com.products.application.dto.*;
import com.products.application.dto.catalogue.ProductSummaryCatalogueResponse;
import com.products.application.dto.catalogue.ProductCatalogueResponse;
import com.products.application.service.ProductCatalogueService;
import com.products.application.service.ProductCategoryService;
import com.products.docs.ProductControllerDocs;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ProductController implements ProductControllerDocs {
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

//    TODO: integration tests for semantic search
    @GetMapping("/products")
    public ResponseEntity<StandardResponse<PagedResponse<ProductSummaryCatalogueResponse>>> getProducts(
            @RequestParam(name = "categoryId", required = false) Integer categoryId,
            @RequestParam(name = "query", required = false) String query,
            Pageable pageable
    ) {
        PagedResponse<ProductSummaryCatalogueResponse> response;

        if (categoryId != null && query != null)
            response = productCatalogueService.semanticSearchByCategoryId(query, categoryId, pageable);

        else if (categoryId != null)
            response = productCatalogueService.getAllByCategoryId(categoryId, pageable);

        else if(query != null)
            response = productCatalogueService.semanticSearch(query, pageable);

        else
            response = productCatalogueService.getAll(pageable);

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
