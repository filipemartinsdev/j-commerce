package com.products.application.service;

import com.products.application.dto.catalogue.CatalogueSearchResponse;
import com.products.application.dto.catalogue.CategoryCatalogueResponse;
import com.products.application.dto.catalogue.ProductCatalogueResponse;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;

import java.util.List;

public interface CatalogueService {
    List<CategoryCatalogueResponse> getAllCategories();

    Window<ProductCatalogueResponse> getAllProducts(ScrollPosition position, Limit limit);

    Window<ProductCatalogueResponse> getAllProductsByCategory(Long categoryId, ScrollPosition position, Limit limit);

    ProductCatalogueResponse getProductById(String id);

    CatalogueSearchResponse search(String query);

    CatalogueSearchResponse search(String query, Long categoryId);
}
