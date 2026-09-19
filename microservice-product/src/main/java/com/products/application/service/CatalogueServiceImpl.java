package com.products.application.service;

import com.products.application.dto.admin.AdminProductResponse;
import com.products.application.dto.catalogue.CatalogueSearchResponse;
import com.products.application.dto.catalogue.CategoryCatalogueResponse;
import com.products.application.dto.catalogue.ProductCatalogueResponse;
import com.products.application.exception.InvalidCatalogueQueryException;
import com.products.application.exception.ProductNotFoundException;
import com.products.application.service.mapper.ProductCatalogueMapper;
import com.products.application.service.mapper.ProductCategoryMapper;
import com.products.infra.persistence.ProductRepository;
import com.products.model.entity.Product;
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
    private final BucketService bucketService;

    public CatalogueServiceImpl(ProductRepository productRepository, ProductEmbeddingService productEmbeddingService, ProductCatalogueMapper productCatalogueMapper, ProductCategoryService productCategoryService, ProductCategoryMapper productCategoryMapper, BucketService bucketService) {
        this.productRepository = productRepository;
        this.productEmbeddingService = productEmbeddingService;
        this.productCatalogueMapper = productCatalogueMapper;
        this.productCategoryService = productCategoryService;
        this.productCategoryMapper = productCategoryMapper;
        this.bucketService = bucketService;
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
                .map(product -> {
                    var dto = productCatalogueMapper.toResponse(product);
                    dto.setImages(getImagesURLs(product.getId(), product.getImages()));
                    return dto;
                });
    }

    private List<ProductCatalogueResponse.ImageResponse> getImagesURLs(String productId, List<Product.ProductImage> images){
        return images.stream()
                .map(image -> {
                    var key = String.format(
                            "products/%s/%s.%s",
                            productId,
                            image.getId(),
                            image.getExtension()
                    );

                    var url = bucketService.getReadPreSignedURL(key);

                    return new ProductCatalogueResponse.ImageResponse(image.getId(), url);
                })
                .toList();
    }


    @Override
    public Window<ProductCatalogueResponse> getAllProductsByCategory(Long categoryId, ScrollPosition scrollPosition, Limit limit) {
        return productRepository.findAllWithPriceByCategory(categoryId, scrollPosition, limit)
                .map(product -> {
                    var dto = productCatalogueMapper.toResponse(product);
                    dto.setImages(getImagesURLs(product.getId(), product.getImages()));
                    return dto;
                });

    }

    @Override
    public ProductCatalogueResponse getProductById(String id) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found by ID: "+id));

        var dto = productCatalogueMapper.toResponse(product);
        dto.setImages(getImagesURLs(product.getId(), product.getImages()));

        return dto;
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
                        .map(product -> {
                            var dto = productCatalogueMapper.toResponse(product);
                            dto.setImages(getImagesURLs(product.getId(), product.getImages()));
                            return dto;
                        })
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
                        .map(product -> {
                            var dto = productCatalogueMapper.toResponse(product);
                            dto.setImages(getImagesURLs(product.getId(), product.getImages()));
                            return dto;
                        })
                        .toList()
        );
    }
}
