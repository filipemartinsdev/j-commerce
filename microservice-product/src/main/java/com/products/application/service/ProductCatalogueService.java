package com.products.application.service;

import com.products.application.dto.*;
import com.products.application.dto.catalogue.ProductPriceCatalogueResponse;
import com.products.application.dto.catalogue.ProductSummaryCatalogueResponse;
import com.products.application.dto.catalogue.ProductCatalogueResponse;
import com.products.application.exception.InvalidProductCategoryException;
import com.products.application.exception.ProductNotFoundException;
import com.products.application.service.mapper.ProductCategoryMapper;
import com.products.application.service.mapper.ProductSKUCatalogueMapper;
import com.products.domain.entity.Product;
import com.products.domain.entity.ProductCategory;
import com.products.domain.entity.ProductResumeCatalogue;
import com.products.domain.entity.ProductSKUSummaryCatalogue;
import com.products.infra.persistence.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProductCatalogueService {
    private final ProductResumeCatalogueRepository productCatalogueResumeRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductCategoryMapper productCategoryMapper;
    private final ProductRepository productRepository;
    private final ProductSKUSummaryCatalogueRepository productSKUSummaryCatalogueRepository;
    private final ProductSKUCatalogueMapper productSKUSummaryCatalogueMapper;
    private final ProductDiscountCalculator productDiscountCalculator;

    public ProductCatalogueService(ProductResumeCatalogueRepository productCatalogueResumeRepository, ProductCategoryRepository productCategoryRepository, ProductCategoryMapper productCategoryMapper, ProductRepository productRepository, ProductSKUSummaryCatalogueRepository productSKUSummaryCatalogueRepository, ProductSKUCatalogueMapper productSKUSummaryCatalogueMapper, ProductDiscountCalculator productDiscountCalculator) {
        this.productCatalogueResumeRepository = productCatalogueResumeRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productCategoryMapper = productCategoryMapper;
        this.productRepository = productRepository;
        this.productSKUSummaryCatalogueRepository = productSKUSummaryCatalogueRepository;
        this.productSKUSummaryCatalogueMapper = productSKUSummaryCatalogueMapper;
        this.productDiscountCalculator = productDiscountCalculator;
    }

    public PagedResponse<ProductSummaryCatalogueResponse> getAll(Pageable pageable) {
        Page<ProductResumeCatalogue> page = productCatalogueResumeRepository.findAll(pageable);

        return PagedResponse.<ProductSummaryCatalogueResponse>builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .isLast(page.isLast())
                .content(page.getContent().stream()
                        .map(entity -> createResumeCatalogueResponse(entity))
                        .toList()
                )
                .build();
    }

    private ProductSummaryCatalogueResponse createResumeCatalogueResponse(ProductResumeCatalogue entity) {
        int discountPercent = productDiscountCalculator.getDiscountPercent(
                entity.getOriginalPriceValue(),
                entity.getCurrentPriceValue()
        );

        return new ProductSummaryCatalogueResponse(
                entity.getProductId(),
                entity.getName(),

                new ProductCategoryResponse(
                        entity.getCategoryId(),
                        entity.getCategoryName()
                ),

                new ProductPriceCatalogueResponse(
                        entity.getOriginalPriceValue(),
                        entity.getCurrentPriceValue(),
                        discountPercent,
                        entity.getCurrentPriceTypeName()
                )
        );
    }

    public PagedResponse<ProductSummaryCatalogueResponse> getAllByCategoryId(Integer categoryId, Pageable pageable) {
        if(!productCategoryRepository.existsById(categoryId))
            throw new InvalidProductCategoryException("Invalid product category with ID: "+categoryId);

        Page<ProductResumeCatalogue> page = productCatalogueResumeRepository.findAllByCategoryId(categoryId, pageable);

        return PagedResponse.<ProductSummaryCatalogueResponse>builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .isLast(page.isLast())
                .content(page.getContent().stream()
                        .map(entity -> createResumeCatalogueResponse(entity))
                        .toList()
                )
                .build();
    }

    public PagedResponse<ProductCategoryResponse> getAllCategories(Pageable pageable){
        Page<ProductCategory> page = productCategoryRepository.findAll(pageable);

        return PagedResponse.<ProductCategoryResponse>builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .isLast(page.isLast())
                .content(page.getContent().stream()
                        .map(productCategoryMapper::toResponse)
                        .toList()
                )
                .build();
    }

    public ProductCatalogueResponse getProductSummaryByProductId(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID:"+productId));

        List<ProductSKUSummaryCatalogue> SKUs = productSKUSummaryCatalogueRepository.findAllByProductId(productId);

        return new ProductCatalogueResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                productCategoryMapper.toResponse(product.getCategory()),
                SKUs.stream()
                    .map(entity -> {
                        Integer discountPercent = productDiscountCalculator.getDiscountPercent(entity.getOriginalPrice(), entity.getCurrentPrice());
                        return productSKUSummaryCatalogueMapper.toResponse(entity, discountPercent);
                    })
                    .toList()
        );
    }
}
