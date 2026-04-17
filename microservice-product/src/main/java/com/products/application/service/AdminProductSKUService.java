package com.products.application.service;

import com.products.application.dto.PagedResponse;
import com.products.application.dto.admin.CreateProductSKURequest;
import com.products.application.dto.admin.ProductSKUAdminResponse;
import com.products.application.dto.admin.UpdateProductSKURequest;
import com.products.application.event.ProductSKUCreatedEvent;
import com.products.application.event.ProductSKUDeletedEvent;
import com.products.application.exception.ProductNotActiveException;
import com.products.application.exception.ProductNotFoundException;
import com.products.application.exception.ProductSKUNotFoundException;
import com.products.application.exception.SKUAlreadyInUseException;
import com.products.application.service.mapper.ProductSKUAdminMapper;
import com.products.domain.entity.Product;
import com.products.domain.entity.ProductSKU;
import com.products.infra.persistence.ProductRepository;
import com.products.infra.persistence.ProductSKURepository;
import jakarta.transaction.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AdminProductSKUService {
    private final ProductRepository productRepository;
    private final ProductSKURepository productSKURepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ProductSKUAdminMapper productSKUAdminMapper;

    public AdminProductSKUService(ProductRepository productRepository, ProductSKURepository productSKURepository, ApplicationEventPublisher applicationEventPublisher, ProductSKUAdminMapper productSKUAdminMapper) {
        this.productRepository = productRepository;
        this.productSKURepository = productSKURepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.productSKUAdminMapper = productSKUAdminMapper;
    }

    @Transactional
    public ProductSKUAdminResponse createProductSKU(CreateProductSKURequest request, UUID authenticatedUserId){
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: "+request.productId()));

        if (productSKURepository.existsBySKU(request.SKU()))
            throw new SKUAlreadyInUseException("This SKU is already in use");

        if (!product.isActive())
            throw new ProductNotActiveException("This product is not active");

        ProductSKU sku = new ProductSKU();
        sku.setProduct(product);
        sku.setName(request.name());
        sku.setSKU(request.SKU());

        ProductSKU newSKU = productSKURepository.save(sku);

        var event = new ProductSKUCreatedEvent(
                newSKU,
                authenticatedUserId,
                this
        );

        applicationEventPublisher.publishEvent(event);

        return productSKUAdminMapper.toResponse(newSKU);
    }

    public ProductSKUAdminResponse updateProductSKU(UUID productSKUId, UpdateProductSKURequest request){
        ProductSKU sku = productSKURepository.findActiveById(productSKUId)
                .orElseThrow(() -> new ProductSKUNotFoundException("Product SKU not found with ID: "+productSKUId));

        if(request.name().isPresent())
            sku.setName(request.name().get());

        if (request.SKU().isPresent())
            sku.setSKU(request.SKU().get());

        return productSKUAdminMapper.toResponse(
                productSKURepository.save(sku)
        );
    }

    public PagedResponse<ProductSKUAdminResponse> getAllProductSKUs(Pageable pageable) {
        Page<ProductSKU> page = productSKURepository.findAllActive(pageable);

        return PagedResponse.<ProductSKUAdminResponse>builder()
                .page(page.getNumber())
                .size(page.getSize())
                .isLast(page.isLast())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .content(page.getContent().stream()
                        .map(entity -> productSKUAdminMapper.toResponse(entity))
                        .toList()
                )
                .build();
    }

    public PagedResponse<ProductSKUAdminResponse> getAllProductSKUsByProductId(UUID productId, Pageable pageable){
        Page<ProductSKU> page = productSKURepository.findAllActiveByProductId(productId, pageable);

        return PagedResponse.<ProductSKUAdminResponse>builder()
                .page(page.getNumber())
                .size(page.getSize())
                .isLast(page.isLast())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .content(page.getContent().stream()
                        .map(entity -> productSKUAdminMapper.toResponse(entity))
                        .toList()
                )
                .build();
    }
    public ProductSKUAdminResponse getProductSKUById(UUID productSKUId){
        ProductSKU sku = productSKURepository.findActiveById(productSKUId)
                .orElseThrow(() -> new ProductSKUNotFoundException("Product SKU not found with ID: "+productSKUId));

        return productSKUAdminMapper.toResponse(sku);
    }

    public void deleteProductSKUById(UUID productSKUId){
        ProductSKU sku = productSKURepository.findActiveById(productSKUId)
                .orElseThrow(() -> new ProductSKUNotFoundException("Product SKU not found with ID: "+productSKUId));

        sku.setIsActive(false);
        productSKURepository.save(sku);

        applicationEventPublisher.publishEvent(
                new ProductSKUDeletedEvent(sku.getId(), this)
        );
    }
}
