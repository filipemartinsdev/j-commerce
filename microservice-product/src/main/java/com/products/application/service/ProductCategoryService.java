package com.products.application.service;

import com.products.application.dto.admin.CreateProductCategoryRequest;
import com.products.application.dto.admin.UpdateProductCategoryRequest;
import com.products.domain.entity.ProductCategory;

import java.util.List;
import java.util.UUID;

public interface ProductCategoryService {
    List<ProductCategory> getAll();

    ProductCategory getById(Long id);

    ProductCategory create(CreateProductCategoryRequest request, UUID userId);

    ProductCategory update(Long categoryId, UpdateProductCategoryRequest request, UUID userId);

    void delete(Long categoryId, UUID userId);
}
