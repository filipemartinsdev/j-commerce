package com.products.infra.persistence;

import com.products.domain.entity.SemanticProductCatalogueProjection;
import com.products.domain.entity.SemanticProductCatalogueView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface SemanticProductCatalogueViewRepository extends JpaRepository<SemanticProductCatalogueView, UUID> {
    @Query(
            nativeQuery = true,
            value =
            """
            SELECT
                p.id AS productId,
                p.name AS name,
                p.description AS description,
                p.category_id AS categoryId,
                p.category_name AS categoryName,
                p.original_price_value AS originalPriceValue,
                p.current_price_type_id AS currentPriceTypeId,
                p.current_price_type_name AS currentPriceTypeName,
                p.current_price_value AS currentPriceValue,
                p.stock_count AS stockCount,
                (p.embedding <-> CAST(:query AS VECTOR)) AS distance
            FROM semantic_product_catalogue_view p
            ORDER BY distance, p.id
            """
    )
    Slice<SemanticProductCatalogueProjection> findAllWithoutCursor(@Param("query") float[] query, Pageable pageable);

    @Query(
            nativeQuery = true,
            value =
            """
            SELECT
                p.id AS productId,
                p.name AS name,
                p.description AS description,
                p.category_id AS categoryId,
                p.category_name AS categoryName,
                p.original_price_value AS originalPriceValue,
                p.current_price_type_id AS currentPriceTypeId,
                p.current_price_type_name AS currentPriceTypeName,
                p.current_price_value AS currentPriceValue,
                p.stock_count AS stockCount,
                (p.embedding <-> CAST(:query AS VECTOR)) AS distance
            FROM semantic_product_catalogue_view p
            WHERE (
                ((p.embedding <-> CAST(:query AS VECTOR)) > :lastDistance) 
                OR 
                (((p.embedding <-> CAST(:query AS VECTOR)) = :lastDistance) AND p.id > :lastId)
            )
            ORDER BY distance, p.id
            """
    )
    Slice<SemanticProductCatalogueProjection> findAllWithCursor(
            @Param("query") float[] query,
            @Param("lastId") UUID lastId,
            @Param("lastDistance") float lastDistance,
            Pageable pageable
    );

    @Query(
            nativeQuery = true,
            value =
            """
            SELECT
                p.id AS productId,
                p.name AS name,
                p.description AS description,
                p.category_id AS categoryId,
                p.category_name AS categoryName,
                p.original_price_value AS originalPriceValue,
                p.current_price_type_id AS currentPriceTypeId,
                p.current_price_type_name AS currentPriceTypeName,
                p.current_price_value AS currentPriceValue,
                p.stock_count AS stockCount,
                (p.embedding <-> CAST(:query AS VECTOR)) AS distance
            FROM semantic_product_catalogue_view p
            WHERE p.category_id = :categoryId
            ORDER BY distance, p.id
            """
    )
    Slice<SemanticProductCatalogueProjection> findAllByCategoryWithoutCursor(
            @Param("query") float[] query,
            @Param("categoryId") Integer categoryId,
            Pageable pageable
    );

    @Query(
            nativeQuery = true,
            value =
            """
            SELECT
                p.id AS productId,
                p.name AS name,
                p.description AS description,
                p.category_id AS categoryId,
                p.category_name AS categoryName,
                p.original_price_value AS originalPriceValue,
                p.current_price_type_id AS currentPriceTypeId,
                p.current_price_type_name AS currentPriceTypeName,
                p.current_price_value AS currentPriceValue,
                p.stock_count AS stockCount,
                (p.embedding <-> CAST(:query AS VECTOR)) AS distance
            FROM semantic_product_catalogue_view p
            WHERE p.category_id = :categoryId AND (
                ((p.embedding <-> CAST(:query AS VECTOR)) > :lastDistance) 
                OR 
                (((p.embedding <-> CAST(:query AS VECTOR)) = :lastDistance) AND p.id > :lastId)
            )
            ORDER BY distance, p.id
            """
    )
    Slice<SemanticProductCatalogueProjection> findAllByCategoryWithCursor(
            @Param("query") float[] query,
            @Param("categoryId") Integer categoryId,
            @Param("lastId") UUID lastId,
            @Param("lastDistance") float lastDistance,
            Pageable pageable
    );
}
