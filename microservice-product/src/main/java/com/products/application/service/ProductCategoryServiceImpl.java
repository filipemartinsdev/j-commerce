package com.products.application.service;

import com.products.application.dto.admin.CreateProductCategoryRequest;
import com.products.application.dto.admin.UpdateProductCategoryRequest;
import com.products.application.exception.ProductCategoryNotFoundException;
import com.products.domain.entity.ProductCategory;
import com.products.infra.persistence.ProductCategoryRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProductCategoryServiceImpl implements ProductCategoryService {
    private final String CATEGORY_LIST_CACHE_NAME = "product_categories";
    private final String CATEGORY_BY_ID_CACHE_NAME = "product_category_by_id";

    private final ProductCategoryRepository productCategoryRepository;

    public ProductCategoryServiceImpl(ProductCategoryRepository productCategoryRepository) {
        this.productCategoryRepository = productCategoryRepository;
    }


    @Override
    @Cacheable(
            value = CATEGORY_BY_ID_CACHE_NAME,
            key = "#id",
            cacheManager = "caffeineCacheManager"
    )
    public ProductCategory getById(Long id) {
            return productCategoryRepository.findById(id)
                    .orElseThrow(() -> new ProductCategoryNotFoundException("Product category not found with ID: "+id));
    }

    @Override
    @Cacheable(
            value = CATEGORY_LIST_CACHE_NAME,
            cacheManager = "caffeineCacheManager"
    )
    public List<ProductCategory> getAll(){
        return productCategoryRepository.findAll();
    }


    @Override
    @Cacheable(
            value = CATEGORY_BY_ID_CACHE_NAME,
            key = "#request.id()",
            cacheManager = "caffeineCacheManager"
    )
    @CacheEvict(
            value = CATEGORY_LIST_CACHE_NAME
    )
    public ProductCategory create(CreateProductCategoryRequest request, UUID userId) {
        var category = new ProductCategory();
        category.setId(request.id());
        category.setName(request.name());
        category.setCreatedBy(userId);

        return productCategoryRepository.save(category);
    }

    @Override
    @Cacheable(
            value = CATEGORY_BY_ID_CACHE_NAME,
            key = "#id",
            cacheManager = "caffeineCacheManager"
    )
    @CacheEvict(
            value = CATEGORY_LIST_CACHE_NAME
    )
    public ProductCategory update(Long id, UpdateProductCategoryRequest request, UUID userId) {
//        TODO
        return null;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(
                    value = CATEGORY_LIST_CACHE_NAME
            ),
            @CacheEvict(
                    value = CATEGORY_BY_ID_CACHE_NAME,
                    key = "#id"
            )
    })
    public void delete(Long id, UUID userId) {
        var category = productCategoryRepository.findById(id)
                .orElseThrow(() -> new ProductCategoryNotFoundException("Product category not found by ID: "+id));
        productCategoryRepository.delete(category);
    }
}
