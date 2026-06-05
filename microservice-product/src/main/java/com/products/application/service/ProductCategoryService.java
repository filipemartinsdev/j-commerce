package com.products.application.service;

import com.products.application.dto.ProductCategoryResponse;
import com.products.application.exception.ProductCategoryNotFoundException;
import com.products.application.service.mapper.ProductCategoryMapper;
import com.products.domain.entity.ProductCategory;
import com.products.infra.persistence.ProductCategoryRepository;
import io.github.responsekit.core.PagedResponse;
import io.github.responsekit.spring.PagedResponseFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductCategoryService {
    private final ProductCategoryMapper productCategoryMapper;
    private final ProductCategoryRepository productCategoryRepository;

    public ProductCategoryService(ProductCategoryMapper productCategoryMapper, ProductCategoryRepository productCategoryRepository) {
        this.productCategoryMapper = productCategoryMapper;
        this.productCategoryRepository = productCategoryRepository;
    }

    @Cacheable(value = "product_category_by_id", key = "#id", cacheManager = "caffeineCacheManager")
    public ProductCategoryResponse getById(Integer id) {
        return productCategoryMapper.toResponse(
                productCategoryRepository.findById(id)
                        .orElseThrow(() -> new ProductCategoryNotFoundException("Product category not found with ID: "+id))
        );
    }

    @Cacheable(
            value = "paged_product_categories",
            key = "{#pageable.pageNumber,#pageable.pageSize,#pageable.sort.toString()}",
            cacheManager = "caffeineCacheManager"
    )
    public PagedResponse<ProductCategoryResponse> getAll(Pageable pageable){
        Page<ProductCategory> page = productCategoryRepository.findAll(pageable);

        return PagedResponseFactory.fromPage(page, productCategoryMapper::toResponse);
    }
}
