package com.products.application.service;

import com.products.domain.entity.Product;

import java.util.List;

public interface ProductEmbeddingService {
    SimilarResponse searchSimilar(String query);

    SimilarResponse searchSimilarByCategory(String query, Long categoryId);

    void createFromProduct(Product product);

    void updateFromProduct(Product product);

    void delete(String productId);

    public static record SimilarResponse(
            String similarQuery,
            List<String> similarIDs
    ){}
}
