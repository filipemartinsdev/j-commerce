package com.products.infra.persistence;

import com.products.domain.entity.ProductCatalogueView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ProductCatalogueViewRepository extends JpaRepository<ProductCatalogueView, UUID> {

    @Query("""
        SELECT p FROM ProductCatalogueView p
        WHERE p.productId > :lastId
        ORDER BY p.productId
    """)
    Slice<ProductCatalogueView> findAllWithCursor(@Param("lastId") UUID lastId, Pageable pageable);

    @Query("""
        SELECT p FROM ProductCatalogueView p
        ORDER BY p.productId
    """)
    Slice<ProductCatalogueView> findAllWithoutCursor(Pageable pageable);


    @Query("""
        SELECT p FROM ProductCatalogueView p
        WHERE p.productId > :lastId AND p.categoryId = :categoryId
        ORDER BY p.productId
    """)
    Slice<ProductCatalogueView> findAllByCategoryWithCursor(
            @Param("categoryId") Integer categoryId,
            @Param("lastId") UUID lastId,
            Pageable pageable
    );

    @Query("""
        SELECT p FROM ProductCatalogueView p
        WHERE p.categoryId = :categoryId
        ORDER BY p.productId
    """)
    Slice<ProductCatalogueView> findAllByCategoryWithoutCursor(
            @Param("categoryId") Integer categoryId,
            Pageable pageable
    );
}
