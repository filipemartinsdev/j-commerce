package com.products.infra.web.admin;

import com.products.application.dto.PagedResponse;
import com.products.application.dto.admin.CreateProductSKUPrice;
import com.products.application.dto.admin.ProductSKUPriceResponse;
import com.products.application.dto.StandardResponse;
import com.products.application.dto.admin.UpdateProductSKUPriceRequest;
import com.products.application.service.AdminProductPriceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin/api/v1/prices")
public class AdminPriceController {
    private final AdminProductPriceService productPriceService;

    public AdminPriceController(AdminProductPriceService productPriceService) {
        this.productPriceService = productPriceService;
    }

    @GetMapping
    public ResponseEntity<StandardResponse<PagedResponse<ProductSKUPriceResponse>>> getAllPrices(
            @RequestParam(required = false) UUID productSKUId,
            Pageable pageable
    ){
        PagedResponse<ProductSKUPriceResponse> response;

        if (productSKUId == null)
            response = productPriceService.getAllPrices(pageable);
        else
            response = productPriceService.getAllPricesByProductSKUId(productSKUId, pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<StandardResponse<ProductSKUPriceResponse>> createProductSKUPrice(
            @Valid @RequestBody CreateProductSKUPrice request
    ){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponse.success(productPriceService.create(request)));
    }

    @PatchMapping("/{priceId}")
    public ResponseEntity<StandardResponse<ProductSKUPriceResponse>> updatePriceById(
            @PathVariable UUID priceId,
            @Valid @RequestBody UpdateProductSKUPriceRequest request
    ){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponse.success(productPriceService.update(priceId, request)));
    }

    @DeleteMapping("/{priceId}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID priceId){
        productPriceService.deleteById(priceId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }
}
