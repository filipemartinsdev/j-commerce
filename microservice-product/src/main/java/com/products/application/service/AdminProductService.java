package com.products.application.service;

import com.products.application.dto.PagedResponse;
import com.products.application.dto.admin.*;
import com.products.application.event.ProductSKUCreatedEvent;
import com.products.application.event.ProductSKUDeletedEvent;
import com.products.application.exception.*;
import com.products.application.service.mapper.ProductAdminMapper;
import com.products.application.service.mapper.ProductSKUAdminMapper;
import com.products.domain.entity.Product;
import com.products.domain.entity.ProductCategory;
import com.products.domain.entity.ProductSKU;
import com.products.infra.persistence.ProductCategoryRepository;
import com.products.infra.persistence.ProductRepository;
import com.products.infra.persistence.ProductSKURepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class AdminProductService {
    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductAdminMapper productAdminMapper;
    private final ProductSKURepository productSKURepository;
    private final ProductSKUAdminMapper productSKUAdminMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AdminProductService(ProductRepository productRepository, ProductCategoryRepository productCategoryRepository, ProductAdminMapper productAdminMapper, ProductSKURepository productSKURepository, ProductSKUAdminMapper productSKUAdminMapper, ApplicationEventPublisher applicationEventPublisher) {
        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productAdminMapper = productAdminMapper;
        this.productSKURepository = productSKURepository;
        this.productSKUAdminMapper = productSKUAdminMapper;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public ProductAdminResponse createProduct(CreateProductRequest request){
        ProductCategory category = productCategoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new InvalidProductCategoryException("Invalid product category with ID: "+request.categoryId()));

        Product product = new Product();
        product.setName(request.name());
        product.setCategory(category);
        product.setActive(true);

        if (request.description().isPresent())
            product.setDescription(request.description().get());

        return productAdminMapper.toResponse(
                productRepository.save(product)
        );
    }

    public ProductAdminResponse updateProduct(UUID productId, UpdateProductRequest request){
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: "+productId));

        if (request.description().isPresent())
            product.setDescription(request.description().get());

        if (request.name().isPresent())
            product.setName(request.name().get());

        if (request.categoryId().isPresent())
            product.setCategory(productCategoryRepository.getReferenceById(request.categoryId().get()));

        return productAdminMapper.toResponse(
                productRepository.save(product)
        );
    }

    public PagedResponse<ProductAdminResponse> getAllProducts(Pageable pageable){
        Page<Product> page = productRepository.findAll(pageable);

        return PagedResponse.<ProductAdminResponse>builder()
                .page(page.getNumber())
                .size(page.getSize())
                .isLast(page.isLast())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .content(page.getContent().stream()
                        .map(entity -> productAdminMapper.toResponse(entity))
                        .toList()
                )
                .build();
    }

    public ProductAdminResponse getProductById(UUID productId){
        return productAdminMapper.toResponse(
                productRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: "+productId))
        );
    }

    public void deleteProductById(UUID productId){
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: "+productId));

        boolean haveActiveSKU = false;

        for (ProductSKU sku : product.getSKUs()) {
            if (sku.getIsActive()) {
                haveActiveSKU = true;
                break;
            }
        }

        if (haveActiveSKU)
            throw new CantDeleteProductException("This product can't be deleted because have an active SKU");

        product.setActive(false);
        productRepository.save(product);
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

        log.info("Event published: {}", event);

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
