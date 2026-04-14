package com.products.application.service;

import com.products.application.dto.PagedResponse;
import com.products.application.dto.admin.CreateStockEntryRequest;
import com.products.application.dto.admin.ProductStockResponse;
import com.products.application.dto.admin.StockMovementResponse;
import com.products.application.exception.ProductSKUNotFoundException;
import com.products.application.exception.ProductStockNotFoundException;
import com.products.application.service.mapper.ProductStockMapper;
import com.products.application.service.mapper.StockMovementMapper;
import com.products.domain.entity.ProductSKU;
import com.products.domain.entity.ProductStock;
import com.products.domain.entity.StockMovement;
import com.products.domain.entity.StockMovementType;
import com.products.infra.persistence.ProductSKURepository;
import com.products.infra.persistence.ProductStockRepository;
import com.products.infra.persistence.StockMovementRepository;
import com.products.infra.persistence.StockMovementTypeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProductStockService {
    private final ProductStockRepository productStockRepository;
    private final ProductStockMapper productStockMapper;
    private final StockMovementTypeRepository stockMovementTypeRepository;
    private final StockMovementRepository stockMovementRepository;
    private final StockMovementMapper stockMovementMapper;
    private final ProductSKURepository productSKURepository;

    public ProductStockService(ProductStockRepository productStockRepository, ProductStockMapper productStockMapper, StockMovementTypeRepository stockMovementTypeRepository, StockMovementRepository stockMovementRepository, StockMovementMapper stockMovementMapper, ProductSKURepository productSKURepository) {
        this.productStockRepository = productStockRepository;
        this.productStockMapper = productStockMapper;
        this.stockMovementTypeRepository = stockMovementTypeRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.stockMovementMapper = stockMovementMapper;
        this.productSKURepository = productSKURepository;
    }

    public PagedResponse<ProductStockResponse> getAll(Pageable pageable){
        Page<ProductStock> page = productStockRepository.findAllActive(pageable);

        return PagedResponse.<ProductStockResponse>builder()
                .page(page.getNumber())
                .size(page.getSize())
                .isLast(page.isLast())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .content(page.getContent().stream()
                        .map(entity -> productStockMapper.toResponse(entity))
                        .toList()
                )
                .build();
    }

    public PagedResponse<ProductStockResponse> getAllByProductId(UUID productId, Pageable pageable){
        Page<ProductStock> page = productStockRepository.findAllActiveByProductId(productId, pageable);

        return PagedResponse.<ProductStockResponse>builder()
                .page(page.getNumber())
                .size(page.getSize())
                .isLast(page.isLast())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .content(page.getContent().stream()
                        .map(entity -> productStockMapper.toResponse(entity))
                        .toList()
                )
                .build();
    }

    public ProductStockResponse getById(UUID id) {
        return productStockMapper.toResponse(
                productStockRepository.findActiveById(id)
                        .orElseThrow(() -> new ProductStockNotFoundException("ProductStock not found with ID: "+id))
        );
    }

    @Transactional
    public void createStockEntry(CreateStockEntryRequest request, UUID authenticatedUserId){
        ProductStock stock = productStockRepository.findByProductSKU_id(request.productSKUId())
                .orElseThrow(() ->  new ProductStockNotFoundException("ProductStock not found with SKU ID: "+request.productSKUId()));

        stock.setUnits(stock.getUnits() + request.units());
        productStockRepository.save(stock);

        ProductSKU sku = productSKURepository.findById(request.productSKUId())
                .orElseThrow(() -> new ProductSKUNotFoundException("ProductSKU not found with ID: "+request.productSKUId()));

        StockMovement stockMovement = new StockMovement();
        stockMovement.setProductSKU(sku);
        stockMovement.setUnits(request.units());
        stockMovement.setType(stockMovementTypeRepository.getReferenceById(StockMovementType.Value.ENTRY.getId()));
        stockMovement.setCreatedBy(authenticatedUserId);
        stockMovementRepository.save(stockMovement);
    }

    public void createStockToSKU(ProductSKU sku, UUID userId) {
        ProductStock stock = new ProductStock();
        stock.setProductSKU(sku);
        stock.setCreatedBy(userId);
        productStockRepository.save(stock);
    }

    public PagedResponse<StockMovementResponse> getAllMovements(Pageable pageable) {
        Page<StockMovement> page = stockMovementRepository.findAll(pageable);

        return PagedResponse.<StockMovementResponse>builder()
                .page(page.getNumber())
                .size(page.getSize())
                .isLast(page.isLast())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .content(page.getContent().stream()
                        .map(entity -> stockMovementMapper.toResponse(entity))
                        .toList()
                )
                .build();
    }

    public PagedResponse<StockMovementResponse> getAllMovementsByProductSKUId(UUID productSKUId, Pageable pageable) {
        Page<StockMovement> page = stockMovementRepository.findAllByProductSKU_id(productSKUId, pageable);

        return PagedResponse.<StockMovementResponse>builder()
                .page(page.getNumber())
                .size(page.getSize())
                .isLast(page.isLast())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .content(page.getContent().stream()
                        .map(entity -> stockMovementMapper.toResponse(entity))
                        .toList()
                )
                .build();
    }

    public void deleteByProductSKUId(UUID productSKUId) {
        ProductStock stock = productStockRepository.findByProductSKU_id(productSKUId)
                .orElseThrow(() -> new ProductStockNotFoundException("ProductStock not found with SKU ID: "+productSKUId));
        stock.setIsActive(false);
        productStockRepository.save(stock);
    }
}
