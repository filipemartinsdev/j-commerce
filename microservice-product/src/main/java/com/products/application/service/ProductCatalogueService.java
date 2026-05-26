package com.products.application.service;

import com.products.application.dto.*;
import com.products.application.dto.catalogue.ProductPriceCatalogueResponse;
import com.products.application.dto.catalogue.ProductSummaryCatalogueResponse;
import com.products.application.dto.catalogue.ProductCatalogueResponse;
import com.products.application.exception.ProductNotFoundException;
import com.products.application.factory.PagedResponseFactory;
import com.products.application.service.mapper.ProductCategoryMapper;
import com.products.application.service.mapper.ProductSKUCatalogueMapper;
import com.products.domain.entity.*;
import com.products.infra.persistence.*;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProductCatalogueService {
    private final ProductCatalogueViewRepository productCatalogueResumeRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductCategoryMapper productCategoryMapper;
    private final ProductRepository productRepository;
    private final ProductCatalogueSummaryViewRepository productCatalogueSummaryViewRepository;
    private final ProductSKUCatalogueMapper productSKUSummaryCatalogueMapper;
    private final ProductDiscountCalculator productDiscountCalculator;
    private final PagedResponseFactory<ProductSummaryCatalogueResponse> pagedResponseFactory;
    private final SemanticProductCatalogueViewRepository semanticProductCatalogueRepository;
    private final EmbeddingModel embeddingModel;

    public ProductCatalogueService(ProductCatalogueViewRepository productCatalogueResumeRepository, ProductCategoryRepository productCategoryRepository, ProductCategoryMapper productCategoryMapper, ProductRepository productRepository, ProductCatalogueSummaryViewRepository productCatalogueSummaryViewRepository, ProductSKUCatalogueMapper productSKUSummaryCatalogueMapper, ProductDiscountCalculator productDiscountCalculator, PagedResponseFactory<ProductSummaryCatalogueResponse> pagedResponseFactory, SemanticProductCatalogueViewRepository semanticProductCatalogueRepository, EmbeddingModel embeddingModel) {
        this.productCatalogueResumeRepository = productCatalogueResumeRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productCategoryMapper = productCategoryMapper;
        this.productRepository = productRepository;
        this.productCatalogueSummaryViewRepository = productCatalogueSummaryViewRepository;
        this.productSKUSummaryCatalogueMapper = productSKUSummaryCatalogueMapper;
        this.productDiscountCalculator = productDiscountCalculator;
        this.pagedResponseFactory = pagedResponseFactory;
        this.semanticProductCatalogueRepository = semanticProductCatalogueRepository;
        this.embeddingModel = embeddingModel;
    }

    public PagedResponse<ProductSummaryCatalogueResponse> getAll(Pageable pageable) {
        Page<ProductCatalogueView> page = productCatalogueResumeRepository.findAll(pageable);

        return pagedResponseFactory.fromPage(page, this::createResumeCatalogueResponse);
    }

    private ProductSummaryCatalogueResponse createResumeCatalogueResponse(ProductCatalogueView entity) {
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
        Page<ProductCatalogueView> page = productCatalogueResumeRepository.findAllByCategoryId(categoryId, pageable);

        return pagedResponseFactory.fromPage(page, this::createResumeCatalogueResponse);
    }

    public ProductCatalogueResponse getProductSummaryByProductId(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID:"+productId));

        List<ProductCatalogueSummaryView> SKUs = productCatalogueSummaryViewRepository.findAllByProductId(productId);

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

    public PagedResponse<ProductSummaryCatalogueResponse> semanticSearch(String query, Pageable pageable) {
        float[] vector = embeddingModel.embed(query);

        return pagedResponseFactory.fromPage(
                semanticProductCatalogueRepository.findAll(vector, pageable),
                this::createResumeCatalogueResponse
        );
    }

    private ProductSummaryCatalogueResponse createResumeCatalogueResponse(SemanticProductCatalogueView entity) {
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

}
