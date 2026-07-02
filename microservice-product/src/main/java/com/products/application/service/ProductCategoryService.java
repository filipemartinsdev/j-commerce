package com.products.application.service;

import com.products.application.dto.ProductCategoryResponse;
import com.products.application.dto.catalogue.CatalogueCategoriesCursor;
import com.products.application.dto.catalogue.CatalogueCursor;
import com.products.application.exception.ProductCategoryNotFoundException;
import com.products.application.service.mapper.ProductCategoryMapper;
import com.products.domain.entity.ProductCategory;
import com.products.infra.persistence.ProductCategoryRepository;
import io.github.responsekit.core.PagedResponse;
import io.github.responsekit.core.SlicedResponse;
import io.github.responsekit.spring.PagedResponseFactory;
import io.github.responsekit.spring.SlicedResponseFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
public class ProductCategoryService {
    private final ProductCategoryMapper productCategoryMapper;
    private final ProductCategoryRepository productCategoryRepository;
    private final CursorCodec cursorCodec;

    public ProductCategoryService(ProductCategoryMapper productCategoryMapper, ProductCategoryRepository productCategoryRepository, CursorCodec cursorCodec) {
        this.productCategoryMapper = productCategoryMapper;
        this.productCategoryRepository = productCategoryRepository;
        this.cursorCodec = cursorCodec;
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
            key = "{#opaqueCursor,#size}",
            cacheManager = "caffeineCacheManager"
    )
    public SlicedResponse<ProductCategoryResponse> getAll(String opaqueCursor, int size){
        Slice<ProductCategory> slice;

        if (opaqueCursor == null)
            slice = productCategoryRepository.findAllWithoutCursor(PageRequest.of(0, size));
        else {
            CatalogueCategoriesCursor cursor = cursorCodec.decode(opaqueCursor, CatalogueCategoriesCursor.class);
            slice = productCategoryRepository.findAllWithCursor(cursor.lastId(), PageRequest.of(0, size));
        }

        return SlicedResponseFactory.fromSlice(
                slice,
                productCategoryMapper::toResponse,
                entity -> cursorCodec.encode(new CatalogueCategoriesCursor(entity.getId()))
        );
    }
}
