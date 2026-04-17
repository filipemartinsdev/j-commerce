package com.products.application.service;

import com.products.application.dto.*;
import com.products.application.dto.catalogue.ProductPriceCatalogueResponse;
import com.products.application.dto.catalogue.ProductResumeCatalogueResponse;
import com.products.application.dto.catalogue.ProductSummaryCatalogueResponse;
import com.products.application.exception.InvalidProductCategoryException;
import com.products.application.exception.ProductNotFoundException;
import com.products.application.service.mapper.ProductCategoryMapper;
import com.products.application.service.mapper.ProductSKUSummaryCatalogueMapper;
import com.products.domain.entity.Product;
import com.products.domain.entity.ProductCategory;
import com.products.domain.entity.ProductResumeCatalogue;
import com.products.domain.entity.ProductSKUSummaryCatalogue;
import com.products.infra.persistence.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
public class ProductCatalogueService {
    private final ProductResumeCatalogueRepository productCatalogueResumeRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductCategoryMapper productCategoryMapper;
    private final ProductRepository productRepository;
    private final ProductSKUSummaryCatalogueRepository productSKUSummaryCatalogueRepository;
    private final ProductSKUSummaryCatalogueMapper productSKUSummaryCatalogueMapper;

    public ProductCatalogueService(ProductResumeCatalogueRepository productCatalogueResumeRepository, ProductCategoryRepository productCategoryRepository, ProductCategoryMapper productCategoryMapper, ProductRepository productRepository, ProductSKUSummaryCatalogueRepository productSKUSummaryCatalogueRepository, ProductSKUSummaryCatalogueMapper productSKUSummaryCatalogueMapper) {
        this.productCatalogueResumeRepository = productCatalogueResumeRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productCategoryMapper = productCategoryMapper;
        this.productRepository = productRepository;
        this.productSKUSummaryCatalogueRepository = productSKUSummaryCatalogueRepository;
        this.productSKUSummaryCatalogueMapper = productSKUSummaryCatalogueMapper;
    }

    public PagedResponse<ProductResumeCatalogueResponse> getAll(Pageable pageable) {
        Page<ProductResumeCatalogue> page = productCatalogueResumeRepository.findAll(pageable);

        return PagedResponse.<ProductResumeCatalogueResponse>builder()
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

    private ProductResumeCatalogueResponse createResumeCatalogueResponse(ProductResumeCatalogue entity) {
        int discountPercent = getDiscountPercent(
                entity.getOriginalPriceValue(),
                entity.getCurrentPriceValue()
        );

        return new ProductResumeCatalogueResponse(
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

    private int getDiscountPercent(BigDecimal originalValue, BigDecimal offerValue){
        if (originalValue == null || offerValue == null || originalValue.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }

        if (originalValue.compareTo(offerValue) == 0) {
            return 0;
        }

        BigDecimal ratio = offerValue.divide(originalValue, 4, RoundingMode.HALF_UP);
        BigDecimal percentage = ratio.multiply(BigDecimal.valueOf(100));

        return 100 - percentage.intValue();
    }

    public PagedResponse<ProductResumeCatalogueResponse> getAllByCategoryId(Integer categoryId, Pageable pageable) {
        if(!productCategoryRepository.existsById(categoryId))
            throw new InvalidProductCategoryException("Invalid product category with ID: "+categoryId);

        Page<ProductResumeCatalogue> page = productCatalogueResumeRepository.findAllByCategoryId(categoryId, pageable);

        return PagedResponse.<ProductResumeCatalogueResponse>builder()
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

    public ProductSummaryCatalogueResponse getProductSummaryByProductId(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID:"+productId));

        List<ProductSKUSummaryCatalogue> SKUs = productSKUSummaryCatalogueRepository.findAllByProductId(productId);

        return new ProductSummaryCatalogueResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                productCategoryMapper.toResponse(product.getCategory()),
                SKUs.stream()
                    .map(entity -> {
                        Integer discountPercent = getDiscountPercent(entity.getOriginalPrice(), entity.getCurrentPrice());
                        return productSKUSummaryCatalogueMapper.toResponse(entity, discountPercent);
                    })
                    .toList()
        );
    }
}
