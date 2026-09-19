package com.products.application.service;

import com.products.application.dto.admin.*;
import com.products.application.message.PriceUpdatedMessage;
import com.products.model.entity.Product;
import com.products.model.entity.ProductCategory;
import com.products.application.dto.admin.UpdateProductSKURequest;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    Window<AdminProductResponse> getAllProducts(ScrollPosition position, Limit limit);

    Window<AdminProductResponse> getAllProductsByCategory(Long categoryId, ScrollPosition position, Limit limit);

    AdminProductResponse getProductById(String id);

    AdminProductResponse createProduct(CreateProductRequest request, UUID userId);

    AdminProductResponse updateProduct(String id, UpdateProductRequest request, UUID userId);

    void deleteProduct(String id, UUID userId);

    List<ProductCategory> getAllCategories();

    ProductCategory createProductCategory(CreateProductCategoryRequest request, UUID userId);

    AdminProductResponse createSKU(String productId, CreateProductSKURequest request, UUID userId);

    AdminProductResponse updateSKU(String SKU, UpdateProductSKURequest request, UUID userId);

    AdminProductResponse deleteSKU(String SKU, UUID userId);

    void updatePrice(PriceUpdatedMessage message);

    UploadImageResponse uploadImage(String productId, String contentType, UUID userId);

    void deleteImage(String productId, UUID imageId, UUID userId);
}
