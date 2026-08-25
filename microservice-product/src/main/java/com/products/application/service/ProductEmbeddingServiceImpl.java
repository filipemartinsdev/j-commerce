package com.products.application.service;

import com.products.application.exception.ProductEmbeddingNotFoundException;
import com.products.domain.entity.Product;
import com.products.domain.entity.ProductEmbedding;
import com.products.domain.projection.ProductEmbeddingProjection;
import com.products.infra.persistence.ProductEmbeddingRepository;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

@Service
public class ProductEmbeddingServiceImpl implements ProductEmbeddingService {
    private final ProductEmbeddingRepository productEmbeddingRepository;
    private final EmbeddingModel embeddingModel;

    public ProductEmbeddingServiceImpl(ProductEmbeddingRepository productEmbeddingRepository, EmbeddingModel embeddingModel) {
        this.productEmbeddingRepository = productEmbeddingRepository;
        this.embeddingModel = embeddingModel;
    }

    //    TODO: implement similar query
    @Override
    public SimilarResponse searchSimilar(String query) {
        float[] vector = embeddingModel.embed(query);

        return new SimilarResponse(
                "",
                productEmbeddingRepository.find20Nearliest(vector).stream()
                        .map(ProductEmbeddingProjection::getProductId)
                        .toList()
        );
    }

    @Override
    public SimilarResponse searchSimilarByCategory(String query, Long categoryId) {
        float[] vector = embeddingModel.embed(query);

        return new SimilarResponse(
                "",
                productEmbeddingRepository.find20NearliestByCategory(vector, categoryId).stream()
                        .map(ProductEmbeddingProjection::getProductId)
                        .toList()
        );
    }

    @Override
    public void createFromProduct(Product product) {
        var productEmbedding = new ProductEmbedding();
        productEmbedding.setProductId(product.getId());
        productEmbedding.setCategoryId(product.getCategory().getId());
        productEmbedding.setEmbedding(embeddingModel.embed(
                getTextForEmbedding(product)
        ));

        productEmbeddingRepository.save(productEmbedding);
    }

    private String getTextForEmbedding(Product product) {
        if (product.getDescription() != null)
            return product.getName() + ". " + product.getDescription() + ".";
        else
            return product.getName();
    }

    @Override
    public void updateFromProduct(Product product) {
        var productEmbedding = productEmbeddingRepository.findByProductId(product.getId())
                .orElseThrow(() -> new ProductEmbeddingNotFoundException("Product embedding not found by product ID: "+product.getId()));

        productEmbedding.setEmbedding(embeddingModel.embed(
                getTextForEmbedding(product)
        ));

        productEmbeddingRepository.save(productEmbedding);
    }

    @Override
    public void delete(String productId) {
        productEmbeddingRepository.deleteByProductId(productId);
    }
}
