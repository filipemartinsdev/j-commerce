package com.products.application.service;

import com.products.application.dto.admin.*;
import com.products.application.exception.ProductNotFoundException;
import com.products.application.exception.ProductSKUNotFoundException;
import com.products.application.exception.SKUAlreadyExistsException;
import com.products.application.message.PriceUpdatedMessage;
import com.products.application.message.SKUCreatedMessage;
import com.products.application.message.SKUDeletedMessage;
import com.products.application.service.mapper.ProductAdminMapper;
import com.products.infra.messaging.MessageBrokerProducer;
import com.products.infra.persistence.ProductRepository;
import com.products.model.entity.Product;
import com.products.model.entity.ProductCategory;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductCategoryService productCategoryService;
    private final ProductEmbeddingService productEmbeddingService;
    private final MessageBrokerProducer messageBrokerProducer;

    private final BucketService bucketService;
    private final ProductAdminMapper productAdminMapper;

    public ProductServiceImpl(ProductRepository productRepository, ProductCategoryService productCategoryService, ProductEmbeddingService productEmbeddingService, MessageBrokerProducer messageBrokerProducer, BucketService bucketService, ProductAdminMapper productAdminMapper) {
        this.productRepository = productRepository;
        this.productCategoryService = productCategoryService;
        this.productEmbeddingService = productEmbeddingService;
        this.messageBrokerProducer = messageBrokerProducer;
        this.bucketService = bucketService;
        this.productAdminMapper = productAdminMapper;
    }


    private List<AdminProductResponse.ImageResponse> getImagesURLs(String productId, List<Product.ProductImage> images){
        return images.stream()
                .map(image -> {
                        var key = String.format(
                                "products/%s/%s.%s",
                                productId,
                                image.getId(),
                                image.getExtension()
                        );

                        var url = bucketService.getReadPreSignedURL(key);

                        return new AdminProductResponse.ImageResponse(image.getId(), url);
                })
                .toList();
    }

    @Override
    public Window<AdminProductResponse> getAllProducts(ScrollPosition position, Limit limit) {
        return productRepository.findAllByOrderById(position, limit)
                .map(product -> {
                    var dto = productAdminMapper.toResponse(product);
                    dto.setImages(
                            getImagesURLs(product.getId(), product.getImages())
                    );
                    return dto;
                });
    }

    @Override
    public Window<AdminProductResponse> getAllProductsByCategory(Long categoryId, ScrollPosition position, Limit limit) {
        return productRepository.findAllByCategoryIdOrderById(categoryId, position, limit)
                .map(product -> {
                    var dto = productAdminMapper.toResponse(product);
                    dto.setImages(
                            getImagesURLs(product.getId(), product.getImages())
                    );
                    return dto;
                });
    }

    @Override
    public AdminProductResponse getProductById(String id) {
        return productRepository.findById(id)
                .map(product -> {
                    var dto = productAdminMapper.toResponse(product);
                    dto.setImages(
                            getImagesURLs(product.getId(), product.getImages())
                    );
                    return dto;
                })
                .orElseThrow(() -> new ProductNotFoundException("Product not found by ID: "+id));
    }

    @Override
    @Transactional
    public AdminProductResponse createProduct(CreateProductRequest request, UUID userId) {
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

        return productAdminMapper.toResponse(createdProduct);
    }

    @Override
    @Transactional
    public AdminProductResponse updateProduct(String id, UpdateProductRequest request, UUID userId) {
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


        var dto = productAdminMapper.toResponse(updatedProduct);
        dto.setImages(
                getImagesURLs(updatedProduct.getId(), updatedProduct.getImages())
        );
        return dto;
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
    public AdminProductResponse createSKU(String productId, CreateProductSKURequest request, UUID userId) {
        var product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found by ID: "+productId));

        if (product.hasSKU(request.SKU()))
            throw new SKUAlreadyExistsException("sku already exists");

        var SKU = assembleSKU(request, userId);
        product.getSKUs().add(SKU);

        var updatedProduct = productRepository.save(product);

        produceSKUCreated(request.SKU());

        var dto = productAdminMapper.toResponse(updatedProduct);
        dto.setImages(
                getImagesURLs(product.getId(), product.getImages())
        );
        return dto;
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
    public AdminProductResponse updateSKU(String SKU, UpdateProductSKURequest request, UUID userId) {
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

        var dto = productAdminMapper.toResponse(productRepository.save(product));
        dto.setImages(
                getImagesURLs(product.getId(), product.getImages())
        );
        return dto;
    }

    @Override
    public AdminProductResponse deleteSKU(String SKU, UUID userId) {
        var product = productRepository.findBySKU(SKU)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with sku: "+SKU));

        boolean removed = product.getSKUs()
                .removeIf(element -> element.getSKU().equals(SKU));

        if (removed) {
            var updatedProduct = productRepository.save(product);
            produceSKUDeleted(SKU);

            var dto = productAdminMapper.toResponse(updatedProduct);
            dto.setImages(
                    getImagesURLs(updatedProduct.getId(), updatedProduct.getImages())
            );
            return dto;
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

    @Override
    @Transactional
    public UploadImageResponse uploadImage(String productId, String contentType, UUID userId) {
        var imageId = UUID.randomUUID();

        var product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found by ID: "+productId));

        product.getImages().add(new Product.ProductImage(imageId, getFileExtension(contentType)));
        product.setUpdatedAt(Instant.now());
        product.setUpdatedBy(userId);

        var key = String.format(
                "products/%s/%s.%s",
                productId,
                imageId,
                getFileExtension(contentType)
        );

        URL presignedURL = bucketService.getUploadPreSignedURL(key);
        productRepository.save(product);

        return new UploadImageResponse(imageId, presignedURL);
    }

    private String getFileExtension(String contentType){
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> throw new IllegalArgumentException("Unsupported content type: " + contentType);
        };
    }

    @Override
    @Transactional
    public void deleteImage(String productId, UUID imageId, UUID userId) {
        bucketService.deleteAllObjectsByPrefix("products/" + productId + "/" + imageId);

        var product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found by ID: " + productId));

        product.getImages().removeIf(image -> image.getId().equals(imageId));
        product.setUpdatedAt(Instant.now());
        product.setUpdatedBy(userId);

        productRepository.save(product);
    }
}
