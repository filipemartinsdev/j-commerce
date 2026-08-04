package com.products.application.service;

import com.products.application.dto.catalogue.CatalogueSearchResponse;
import com.products.application.dto.catalogue.CategoryCatalogueResponse;
import com.products.application.dto.catalogue.ProductCatalogueResponse;
import com.products.application.exception.InvalidCatalogueQueryException;
import com.products.application.exception.ProductNotFoundException;
import com.products.application.service.mapper.ProductCatalogueMapper;
import com.products.application.service.mapper.ProductCategoryMapper;
import com.products.infra.persistence.ProductRepository;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogueServiceImpl implements CatalogueService {
    private final ProductRepository productRepository;
    private final ProductEmbeddingService productEmbeddingService;
    private final ProductCatalogueMapper productCatalogueMapper;
    private final ProductCategoryService productCategoryService;
    private final ProductCategoryMapper productCategoryMapper;

    public CatalogueServiceImpl(ProductRepository productRepository, ProductEmbeddingService productEmbeddingService, ProductCatalogueMapper productCatalogueMapper, ProductCategoryService productCategoryService, ProductCategoryMapper productCategoryMapper) {
        this.productRepository = productRepository;
        this.productEmbeddingService = productEmbeddingService;
        this.productCatalogueMapper = productCatalogueMapper;
        this.productCategoryService = productCategoryService;
        this.productCategoryMapper = productCategoryMapper;
    }


    @Override
    public List<CategoryCatalogueResponse> getAllCategories() {
        return productCategoryService.getAll()
                .stream()
                .map(productCategoryMapper::toResponse)
                .toList();
    }

    @Override
    public Window<ProductCatalogueResponse> getAllProducts(ScrollPosition scrollPosition, Limit limit) {
        return productRepository.findAllWithPrice(scrollPosition, limit)
                .map(productCatalogueMapper::toResponse);
    }

    @Override
    public Window<ProductCatalogueResponse> getAllProductsByCategory(Long categoryId, ScrollPosition scrollPosition, Limit limit) {
        return productRepository.findAllWithPriceByCategory(categoryId, scrollPosition, limit)
                .map(productCatalogueMapper::toResponse);
    }

    @Override
    public ProductCatalogueResponse getProductById(String id) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found by ID: "+id));
        return productCatalogueMapper.toResponse(product);
    }

    @Override
    public CatalogueSearchResponse search(String query) {
        if (query == null || query.isEmpty())
            throw new InvalidCatalogueQueryException("Query must not be empty");

        ProductEmbeddingService.SimilarResponse similarResponse = productEmbeddingService.searchSimilar(query);

        return new CatalogueSearchResponse(
                similarResponse.similarQuery(),
                productRepository.findAllWithPriceById(similarResponse.similarIDs())
                        .stream()
                        .map(productCatalogueMapper::toResponse)
                        .toList()
        );
    }

    @Override
    public CatalogueSearchResponse search(String query, Long categoryId) {
        if (query == null || query.isEmpty())
            throw new InvalidCatalogueQueryException("Query must not be empty");

        ProductEmbeddingService.SimilarResponse similarResponse = productEmbeddingService.searchSimilarByCategory(query, categoryId);

        return new CatalogueSearchResponse(
                similarResponse.similarQuery(),
                productRepository.findAllWithPriceById(similarResponse.similarIDs())
                        .stream()
                        .map(productCatalogueMapper::toResponse)
                        .toList()
        );
    }
}
