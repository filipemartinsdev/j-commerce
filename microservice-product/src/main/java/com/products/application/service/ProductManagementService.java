package com.products.application.service;

import com.products.application.dto.admin.*;
import com.products.application.exception.*;
import com.products.application.service.mapper.ProductAdminMapper;
import com.products.domain.entity.Product;
import com.products.domain.entity.ProductCategory;
import com.products.domain.entity.ProductSKU;
import com.products.infra.persistence.ProductCategoryRepository;
import com.products.infra.persistence.ProductRepository;
import io.github.responsekit.core.PagedResponse;
import io.github.responsekit.spring.PagedResponseFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ProductManagementService {
    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductAdminMapper productAdminMapper;
    private final EmbeddingModel embeddingModel;

    public ProductManagementService(ProductRepository productRepository, ProductCategoryRepository productCategoryRepository, ProductAdminMapper productAdminMapper, EmbeddingModel embeddingModel) {
        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productAdminMapper = productAdminMapper;
        this.embeddingModel = embeddingModel;
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

        product.setEmbedding(getEmbedding(product));

        return productAdminMapper.toResponse(
                productRepository.save(product)
        );
    }

    private float[] getEmbedding(Product product) {
        String text = getTextToEmbedding(product);

        try {
            return embeddingModel.embed(text);
        } catch (Exception e) {
            String messageLog = "Could not get embedding for product with ID: "+product.getId();
            log.error(messageLog, e);
            throw new BadGatewayException();
        }
    }

    private String getTextToEmbedding(Product product) {
        return product.getName() + ". " + product.getDescription();
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

        return PagedResponseFactory.fromPage(page, productAdminMapper::toResponse);
    }

    public PagedResponse<ProductAdminResponse> getAllProductsByCategoryId(Integer categoryId, Pageable pageable){
        Page<Product> page = productRepository.findAllByCategoryId(categoryId, pageable);

        return PagedResponseFactory.fromPage(page, productAdminMapper::toResponse);
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



}
