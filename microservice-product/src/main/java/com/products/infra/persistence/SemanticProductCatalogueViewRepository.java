package com.products.infra.persistence;

import com.products.domain.entity.SemanticProductCatalogueView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface SemanticProductCatalogueViewRepository extends JpaRepository<SemanticProductCatalogueView, UUID> {
    @Query(
            nativeQuery = true,

            value =
            """
            SELECT * FROM semantic_product_catalogue_view p
            ORDER BY p.embedding <-> CAST(:query AS VECTOR)
            """,

            countQuery =
            """
            SELECT COUNT(*) FROM semantic_product_catalogue_view p
            """
    )
    Page<SemanticProductCatalogueView> findAll(@Param("query") float[] query, Pageable pageable);

    @Query(
            nativeQuery = true,

            value =
                    """
                    SELECT * FROM semantic_product_catalogue_view p
                    WHERE p.category_id = :categoryId
                    ORDER BY p.embedding <-> CAST(:query AS VECTOR)
                    """,

            countQuery =
                    """
                    SELECT COUNT(*) FROM semantic_product_catalogue_view p
                    WHERE p.category_id = :categoryId
                    """
    )
    Page<SemanticProductCatalogueView> findAll(
            @Param("query") float[] query,
            @Param("categoryId") Integer categoryId,
            Pageable pageable
    );
}
