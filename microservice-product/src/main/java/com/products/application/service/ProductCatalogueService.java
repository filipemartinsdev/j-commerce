package com.products.application.service;

import com.products.application.dto.*;
import com.products.application.dto.catalogue.*;
import com.products.application.exception.ProductNotFoundException;
import com.products.application.service.mapper.ProductCategoryMapper;
import com.products.application.service.mapper.ProductSKUCatalogueMapper;
import com.products.domain.entity.*;
import com.products.infra.persistence.*;
import io.github.responsekit.core.PagedResponse;
import io.github.responsekit.core.SlicedResponse;
import io.github.responsekit.spring.PagedResponseFactory;
import io.github.responsekit.spring.SlicedResponseFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProductCatalogueService {
    private final ProductCatalogueViewRepository productCatalogueViewRepository;
    private final ProductCategoryMapper productCategoryMapper;
    private final ProductRepository productRepository;
    private final ProductCatalogueSummaryViewRepository productCatalogueSummaryViewRepository;
    private final ProductSKUCatalogueMapper productSKUSummaryCatalogueMapper;
    private final ProductDiscountCalculator productDiscountCalculator;
    private final SemanticProductCatalogueViewRepository semanticProductCatalogueRepository;
    private final EmbeddingModel embeddingModel;
    private final CursorCodec cursorCodec;

    public ProductCatalogueService(ProductCatalogueViewRepository productCatalogueViewRepository, ProductCategoryMapper productCategoryMapper, ProductRepository productRepository, ProductCatalogueSummaryViewRepository productCatalogueSummaryViewRepository, ProductSKUCatalogueMapper productSKUSummaryCatalogueMapper, ProductDiscountCalculator productDiscountCalculator, SemanticProductCatalogueViewRepository semanticProductCatalogueRepository, EmbeddingModel embeddingModel, CursorCodec cursorCodec) {
        this.productCatalogueViewRepository = productCatalogueViewRepository;
        this.productCategoryMapper = productCategoryMapper;
        this.productRepository = productRepository;
        this.productCatalogueSummaryViewRepository = productCatalogueSummaryViewRepository;
        this.productSKUSummaryCatalogueMapper = productSKUSummaryCatalogueMapper;
        this.productDiscountCalculator = productDiscountCalculator;
        this.semanticProductCatalogueRepository = semanticProductCatalogueRepository;
        this.embeddingModel = embeddingModel;
        this.cursorCodec = cursorCodec;
    }

//    public SlicedResponse<ProductSummaryCatalogueResponse> getAll(int limit) {
//        Slice<ProductCatalogueView> slice = productCatalogueViewRepository.findAllWithoutCursor(PageRequest.of(0, limit));
//        return SlicedResponseFactory.fromSlice(
//                slice,
//                this::createCatalogueSummaryResponse,
//                entity -> cursorCodec.encode(new CatalogueCursor(entity.getProductId()))
//        );
//    }

    public SlicedResponse<ProductSummaryCatalogueResponse> getAll(String opaqueCursor, int limit) {
        Slice<ProductCatalogueView> slice;

        if (opaqueCursor == null || opaqueCursor.isEmpty())
            slice = productCatalogueViewRepository.findAllWithoutCursor(PageRequest.of(0, limit));
        else {
            CatalogueCursor cursor = cursorCodec.decode(opaqueCursor, CatalogueCursor.class);
            slice = productCatalogueViewRepository.findAllWithCursor(cursor.lastId(), PageRequest.of(0, limit));
        }

        return SlicedResponseFactory.fromSlice(
                slice,
                this::createCatalogueSummaryResponse,
                entity -> cursorCodec.encode(new CatalogueCursor(entity.getProductId())));
    }

    private ProductSummaryCatalogueResponse createCatalogueSummaryResponse(ProductCatalogueView entity) {
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


    public SlicedResponse<ProductSummaryCatalogueResponse> getAllByCategoryId(Integer categoryId, String opaqueCursor, int size) {
        Slice<ProductCatalogueView> slice;

        if (opaqueCursor == null || opaqueCursor.isEmpty())
            slice = productCatalogueViewRepository.findAllByCategoryWithoutCursor(categoryId, PageRequest.of(0, size));
        else {
            CatalogueCursor cursor = cursorCodec.decode(opaqueCursor, CatalogueCursor.class);
            slice = productCatalogueViewRepository.findAllByCategoryWithCursor(categoryId, cursor.lastId(), PageRequest.of(0, size));
        }

        return SlicedResponseFactory.fromSlice(
                slice,
                this::createCatalogueSummaryResponse,
                entity -> cursorCodec.encode(new CatalogueCursor(entity.getProductId()))
        );
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

    public SlicedResponse<ProductSummaryCatalogueResponse> semanticSearch(String query, String opaqueCursor, int size) {
        float[] vector = embeddingModel.embed(query);

        Slice<SemanticProductCatalogueProjection> slice;

        if(opaqueCursor == null || opaqueCursor.isEmpty())
            slice = semanticProductCatalogueRepository.findAllWithoutCursor(vector, PageRequest.of(0, size));
        else {
            SemanticCatalogueCursor cursor = cursorCodec.decode(opaqueCursor, SemanticCatalogueCursor.class);
            slice = semanticProductCatalogueRepository.findAllWithCursor(vector, cursor.lastId(), cursor.lastDistance(), PageRequest.of(0, size));
        }

        return SlicedResponseFactory.fromSlice(
                slice,
                this::createCatalogueSummaryResponse,
                entity -> cursorCodec.encode(new SemanticCatalogueCursor(entity.getProductId(), entity.getDistance()))
        );
    }

    private ProductSummaryCatalogueResponse createCatalogueSummaryResponse(SemanticProductCatalogueProjection entity) {
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

//    TODO: unit tests
    public SlicedResponse<ProductSummaryCatalogueResponse> semanticSearchByCategoryId(
            String query, Integer categoryId, String opaqueCursor, int size
    ) {
        float[] vector = embeddingModel.embed(query);

        Slice<SemanticProductCatalogueProjection> slice;

        if (opaqueCursor == null || opaqueCursor.isEmpty())
            slice = semanticProductCatalogueRepository.findAllByCategoryWithoutCursor(vector, categoryId, PageRequest.of(0, size));
        else {
            SemanticCatalogueCursor cursor = cursorCodec.decode(opaqueCursor, SemanticCatalogueCursor.class);
            slice = semanticProductCatalogueRepository.findAllByCategoryWithCursor(vector, categoryId, cursor.lastId(), cursor.lastDistance(), PageRequest.of(0, size));
        }

        return SlicedResponseFactory.fromSlice(
                slice,
                this::createCatalogueSummaryResponse,
                entity -> cursorCodec.encode(new SemanticCatalogueCursor(entity.getProductId(), entity.getDistance()))
        );
    }
}
