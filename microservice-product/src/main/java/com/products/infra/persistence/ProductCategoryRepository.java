package com.products.infra.persistence;

import com.products.domain.entity.Product;
import com.products.domain.entity.ProductCategory;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Integer> {
    @Query("""
        SELECT p FROM ProductCategory p
        ORDER BY p.id
    """)
    Slice<ProductCategory> findAllWithoutCursor(Pageable pageable);

    @Query("""
        SELECT p FROM ProductCategory p
        WHERE p.id > :lastId
        ORDER BY p.id
    """)
    Slice<ProductCategory> findAllWithCursor(@Param("lastId") Integer lastId, Pageable pageable);
}
