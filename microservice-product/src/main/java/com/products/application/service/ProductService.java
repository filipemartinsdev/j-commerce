package com.products.application.service;

import com.products.application.dto.admin.*;
import com.products.application.message.PriceUpdatedMessage;
import com.products.domain.entity.Product;
import com.products.domain.entity.ProductCategory;
import com.products.application.dto.admin.UpdateProductSKURequest;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    Window<Product> getAllProducts(ScrollPosition position, Limit limit);

    Window<Product> getAllProductsByCategory(Long categoryId, ScrollPosition position, Limit limit);

    Product getProductById(String id);

    Product createProduct(CreateProductRequest request, UUID userId);

    Product updateProduct(String id, UpdateProductRequest request, UUID userId);

    void deleteProduct(String id, UUID userId);

    List<ProductCategory> getAllCategories();

    ProductCategory createProductCategory(CreateProductCategoryRequest request, UUID userId);

    Product createSKU(String productId, CreateProductSKURequest request, UUID userId);

    Product updateSKU(String SKU, UpdateProductSKURequest request, UUID userId);

    Product deleteSKU(String SKU, UUID userId);

    void updatePrice(PriceUpdatedMessage message);
}
