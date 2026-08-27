package com.products.application.service;

import com.products.application.dto.admin.*;
import com.products.application.exception.ProductNotFoundException;
import com.products.application.exception.ProductSKUNotFoundException;
import com.products.application.exception.SKUAlreadyExistsException;
import com.products.application.message.PriceUpdatedMessage;
import com.products.application.message.SKUCreatedMessage;
import com.products.application.message.SKUDeletedMessage;
import com.products.domain.entity.Product;
import com.products.domain.entity.ProductCategory;
import com.products.infra.messaging.MessageBrokerProducer;
import com.products.infra.persistence.ProductRepository;
import com.products.application.dto.admin.UpdateProductSKURequest;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductCategoryService productCategoryService;
    private final ProductEmbeddingService productEmbeddingService;
    private final MessageBrokerProducer messageBrokerProducer;

    public ProductServiceImpl(ProductRepository productRepository, ProductCategoryService productCategoryService, ProductEmbeddingService productEmbeddingService, MessageBrokerProducer messageBrokerProducer) {
        this.productRepository = productRepository;
        this.productCategoryService = productCategoryService;
        this.productEmbeddingService = productEmbeddingService;
        this.messageBrokerProducer = messageBrokerProducer;
    }

    @Override
    public Window<Product> getAllProducts(ScrollPosition position, Limit limit) {
        return productRepository.findAllByOrderById(position, limit);
    }

    @Override
    public Window<Product> getAllProductsByCategory(Long categoryId, ScrollPosition position, Limit limit) {
        return productRepository.findAllByCategoryIdOrderById(categoryId, position, limit);
    }

    @Override
    public Product getProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found by ID: "+id));
    }

    @Override
    @Transactional
    public Product createProduct(CreateProductRequest request, UUID userId) {
        var product = new Product();
        product.setName(request.name());
        product.setCreatedBy(userId);

        var category = productCategoryService.getById(request.categoryId());

        product.setCategory(new Product.CategorySummary(
                category.getId(),
                category.getName()
        ));

        if(request.description().isPresent())
            product.setDescription(request.description().get());

        var createdProduct = productRepository.save(product);

        productEmbeddingService.createFromProduct(product);

        return createdProduct;
    }

    @Override
    @Transactional
    public Product updateProduct(String id, UpdateProductRequest request, UUID userId) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found by ID: "+id));

        if (request.name().isPresent())
            product.setName(request.name().get());

        if (request.description().isPresent())
            product.setDescription(request.description().get());

        if (request.categoryId().isPresent()){
            var category = productCategoryService.getById(request.categoryId().get());

            product.setCategory(new Product.CategorySummary(
                    category.getId(),
                    category.getName()
            ));
        }

        product.setUpdatedAt(Instant.now());
        product.setUpdatedBy(userId);

        var updatedProduct = productRepository.save(product);

        productEmbeddingService.updateFromProduct(updatedProduct);

        return updatedProduct;
    }

    @Override
    @Transactional
    public void deleteProduct(String id, UUID userId) {
        var product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found by ID: "+id));

        productRepository.delete(product);

        productEmbeddingService.delete(product.getId());

        produceSKUsDeleted(product);
    }

    private void produceSKUsDeleted(Product product) {
        for (Product.ProductSKU productSKU : product.getSKUs())
            messageBrokerProducer.produceSKUDeleted(new SKUDeletedMessage(productSKU.getSKU()));
    }

    @Override
    public List<ProductCategory> getAllCategories() {
        return productCategoryService.getAll();
    }

    @Override
    public ProductCategory createProductCategory(CreateProductCategoryRequest request, UUID userId) {
        return productCategoryService.create(request, userId);
    }

    @Override
    public Product createSKU(String productId, CreateProductSKURequest request, UUID userId) {
        var product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found by ID: "+productId));

        if (product.hasSKU(request.SKU()))
            throw new SKUAlreadyExistsException("sku already exists");

        var SKU = assembleSKU(request, userId);
        product.getSKUs().add(SKU);

        var updatedProduct = productRepository.save(product);

        produceSKUCreated(request.SKU());

        return updatedProduct;
    }

    private Product.ProductSKU assembleSKU(CreateProductSKURequest request, UUID userId) {
        var SKU = new Product.ProductSKU();
        SKU.setSKU(request.SKU());
        SKU.setName(request.name());
        SKU.setAttributes(request.attributes()
                .stream()
                .map(attributeRequest ->
                        new Product.ProductSKU.Attribute(attributeRequest.name(), attributeRequest.value()))
                .toList()
        );
        SKU.setCreatedBy(userId);
        return SKU;
    }

    private void produceSKUCreated(String sku) {
        messageBrokerProducer.produceSKUCreated(new SKUCreatedMessage(sku));
    }


    @Override
    public Product updateSKU(String SKU, UpdateProductSKURequest request, UUID userId) {
        var product = productRepository.findBySKU(SKU)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with sku: "+SKU));

        var productSKU = product.findSKU(SKU)
                .orElseThrow(() -> new ProductSKUNotFoundException("sku not found: "+SKU));

        if(request.name().isPresent())
            productSKU.setName(request.name().get());

        if(request.attributes().isPresent())
            productSKU.setAttributes(request.attributes().get()
                    .stream()
                    .map(attribute -> new Product.ProductSKU.Attribute(attribute.name(), attribute.value()))
                    .toList()
            );

        return productRepository.save(product);
    }

    @Override
    public Product deleteSKU(String SKU, UUID userId) {
        var product = productRepository.findBySKU(SKU)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with sku: "+SKU));

        boolean removed = product.getSKUs()
                .removeIf(element -> element.getSKU().equals(SKU));

        if (removed) {
            var updatedProduct = productRepository.save(product);
            produceSKUDeleted(SKU);
            return updatedProduct;
        }
        else
            throw new ProductSKUNotFoundException("sku not found: "+SKU);
    }

    private void produceSKUDeleted(String sku) {
        messageBrokerProducer.produceSKUDeleted(new SKUDeletedMessage(sku));
    }


    @Override
    public void updatePrice(PriceUpdatedMessage message) {
        var product = productRepository.findBySKU(message.sku())
                .orElseThrow(() -> new ProductNotFoundException("Product not found by sku: " + message.sku()));

        var sku = product.findSKU(message.sku())
                .orElseThrow(() -> new ProductSKUNotFoundException("sku not found: " + message.sku()));

        if(message.basePrice().isPresent())
            sku.setBasePrice(new Product.ProductSKU.Price(
                    message.basePrice().get().label(),
                    message.basePrice().get().value()
            ));
        else
            sku.setBasePrice(null);

        if (message.currentPrice().isPresent())
            sku.setCurrentPrice(new Product.ProductSKU.Price(
                    message.currentPrice().get().label(),
                    message.currentPrice().get().value()
            ));
        else
            sku.setCurrentPrice(null);

        sku.setUpdatedAt(Instant.now());
        sku.setUpdatedBy(null);
        product.setUpdatedAt(Instant.now());
        product.setUpdatedBy(null);

        productRepository.save(product);
    }
}
