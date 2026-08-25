package com.products.infra.persistence;

import com.products.domain.entity.ProductEmbedding;
import com.products.domain.projection.ProductEmbeddingProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ProductEmbeddingRepository extends JpaRepository<ProductEmbedding, UUID> {
    void deleteByProductId(String productId);

    Optional<ProductEmbedding> findByProductId(String id);

    @Query(
            nativeQuery = true,
            value =
            """
            SELECT
                e.id AS id,
                e.product_id AS productId,
                (e.embedding <-> CAST(:query AS VECTOR)) AS distance
            FROM product_embedding e
            ORDER BY distance, e.id
            LIMIT 20
            """
    )
    Slice<ProductEmbeddingProjection> find20Nearliest(@Param("query") float[] query);

    @Query(
            nativeQuery = true,
            value =
            """
            SELECT
                e.id AS id,
                e.product_id AS productId,
                (e.embedding <-> CAST(:query AS VECTOR)) AS distance
            FROM product_embedding e
            WHERE e.category_id = :categoryId
            ORDER BY distance, e.id
            LIMIT 20
            """
    )
    Slice<ProductEmbeddingProjection> find20NearliestByCategory(@Param("query") float[] query, @Param("categoryId") Long categoryId);

    @Deprecated
    @Query(
            nativeQuery = true,
            value =
            """
            SELECT
                e.id AS id,
                e.product_id AS productId,
                (e.embedding <-> CAST(:query AS VECTOR)) AS distance
            FROM product_embedding e
            WHERE (
                ((e.embedding <-> CAST(:query AS VECTOR)) > :lastDistance)
                OR 
                (((e.embedding <-> CAST(:query AS VECTOR)) = :lastDistance) AND e.id > :lastId)
            )
            ORDER BY distance, e.id
            LIMIT 20
            """
    )
    Slice<ProductEmbeddingProjection> findAllWithCursor(
            @Param("query") float[] query,
            @Param("lastId") UUID lastId,
            @Param("lastDistance") float lastDistance,
            Pageable pageable
    );

    @Deprecated
    @Query(
            nativeQuery = true,
            value =
            """
            SELECT
                e.id AS id,
                e.product_id AS productId,
                (e.embedding <-> CAST(:query AS VECTOR)) AS distance
            FROM product_embedding e
            WHERE e.category_id = :categoryId
            ORDER BY distance, e.id
            """
    )
    Slice<ProductEmbeddingProjection> findAllByCategoryWithoutCursor(
            @Param("query") float[] query,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );

    @Deprecated
    @Query(
            nativeQuery = true,
            value =
            """
            SELECT
                e.id AS id,
                e.product_id AS productId,
                (e.embedding <-> CAST(:query AS VECTOR)) AS distance
            FROM product_embedding e
            WHERE e.category_id = :categoryId AND (
                ((e.embedding <-> CAST(:query AS VECTOR)) > :lastDistance)
                OR
                (((e.embedding <-> CAST(:query AS VECTOR)) = :lastDistance) AND e.id > :lastId)
            )
            ORDER BY distance, e.id
            """
    )
    Slice<ProductEmbeddingProjection> findAllByCategoryWithCursor(
            @Param("query") float[] query,
            @Param("categoryId") Long categoryId,
            @Param("lastId") UUID lastId,
            @Param("lastDistance") float lastDistance,
            Pageable pageable
    );
}
