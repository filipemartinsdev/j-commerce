package com.products.infra.web.catalogue;

import com.products.application.dto.ProductCategoryResponse;
import com.products.application.dto.catalogue.ProductCatalogueResponse;
import com.products.application.dto.catalogue.ProductSummaryCatalogueResponse;
import com.products.application.service.CursorCodec;
import com.products.application.service.ProductCatalogueService;
import com.products.application.service.ProductCategoryService;
import com.products.docs.ProductControllerDocs;
import io.github.responsekit.core.PagedResponse;
import io.github.responsekit.core.StandardResponse;
import io.github.responsekit.core.*;
import io.github.responsekit.spring.*;
import jakarta.validation.constraints.Max;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<StandardResponse<SlicedResponse<ProductCategoryResponse>>> getCategories(
            @RequestParam(name = "cursor", required = false) String opaqueCursor,
            Pageable pageable
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        StandardResponse.success(productCategoryService.getAll(opaqueCursor, pageable.getPageSize())).build()
                );
    }

//    TODO: integration tests for semantic search
    @GetMapping("/products")
    public ResponseEntity<StandardResponse<SlicedResponse<ProductSummaryCatalogueResponse>>> getProducts(
            @RequestParam(name = "query", required = false) String query,
            @RequestParam(name = "categoryId", required = false) Integer categoryId,
            @RequestParam(name = "cursor", required = false) String opaqueCursor,
            Pageable pageable
    ) {
        SlicedResponse<ProductSummaryCatalogueResponse> response = resolveResponse(categoryId, query, opaqueCursor, pageable.getPageSize());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponse.success(response).build());
    }

    private SlicedResponse<ProductSummaryCatalogueResponse> resolveResponse (Integer categoryId, String query, String opaqueCursor, int size){
        if (categoryId != null && query != null)
            return productCatalogueService.semanticSearchByCategoryId(query, categoryId, opaqueCursor, size);

        else if (categoryId != null)
            return productCatalogueService.getAllByCategoryId(categoryId, opaqueCursor, size);

        else if(query != null)
            return productCatalogueService.semanticSearch(query, opaqueCursor, size);

        else
            return productCatalogueService.getAll(opaqueCursor, size);
    };

    @GetMapping("/products/{productId}")
    public ResponseEntity<StandardResponse<ProductCatalogueResponse>> getProductById(@PathVariable UUID productId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponse.success(productCatalogueService.getProductSummaryByProductId(productId)).build());
    }
}
