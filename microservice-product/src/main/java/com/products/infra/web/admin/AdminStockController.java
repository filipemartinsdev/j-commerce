package com.products.infra.web.admin;

import com.products.application.dto.PagedResponse;
import com.products.application.dto.StandardResponse;
import com.products.application.dto.admin.CreateStockEntryRequest;
import com.products.application.dto.admin.ProductStockResponse;
import com.products.application.dto.admin.StockMovementResponse;
import com.products.application.dto.admin.StockMovementTypeResponse;
import com.products.application.service.ProductStockManagementService;
import com.products.application.service.StockMovementTypeService;
import com.products.application.service.mapper.StockMovementTypeMapper;
import com.products.infra.persistence.StockMovementTypeRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/api/v1/stock")
public class AdminStockController {
    private final ProductStockManagementService adminProductStockService;
    private final StockMovementTypeRepository stockMovementTypeRepository;
    private final StockMovementTypeMapper stockMovementTypeMapper;
    private final StockMovementTypeService stockMovementTypeService;

    public AdminStockController(ProductStockManagementService adminProductStockService, StockMovementTypeRepository stockMovementTypeRepository, StockMovementTypeMapper stockMovementTypeMapper, StockMovementTypeService stockMovementTypeService) {
        this.adminProductStockService = adminProductStockService;
        this.stockMovementTypeRepository = stockMovementTypeRepository;
        this.stockMovementTypeMapper = stockMovementTypeMapper;
        this.stockMovementTypeService = stockMovementTypeService;
    }

    @GetMapping
    public ResponseEntity<StandardResponse<PagedResponse<ProductStockResponse>>> getAllStock(
            @RequestParam(required = false) UUID productId,
            Pageable pageable
    ){
        PagedResponse<ProductStockResponse> response;

        if (productId == null)
            response = adminProductStockService.getAll(pageable);
        else
            response = adminProductStockService.getAllByProductId(productId, pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponse.success(response));
    }

    @PostMapping("/entries")
    public ResponseEntity<Void> createStockEntry (
            @Valid @RequestBody CreateStockEntryRequest request,
            @AuthenticationPrincipal Jwt jwt
    ){
        UUID authenticatedUserId = UUID.fromString(jwt.getSubject());
        adminProductStockService.createStockEntry(request, authenticatedUserId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @GetMapping("/movements")
    public ResponseEntity<StandardResponse<PagedResponse<StockMovementResponse>>> getAllStockMovements(
            @RequestParam(required = false) UUID productSKUId,
            Pageable pageable
    ){
        PagedResponse<StockMovementResponse> response;

        if (productSKUId == null)
            response = adminProductStockService.getAllMovements(pageable);
        else
            response = adminProductStockService.getAllMovementsByProductSKUId(productSKUId, pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponse.success(response));
    }

    @GetMapping("/movements/types")
    public ResponseEntity<StandardResponse<List<StockMovementTypeResponse>>> getAllStockMovementsTypes(){
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(StandardResponse.success(
                        stockMovementTypeService.getAll()
                ));
    }
}
