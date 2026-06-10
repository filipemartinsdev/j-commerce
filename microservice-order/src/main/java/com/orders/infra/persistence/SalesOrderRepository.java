package com.orders.infra.persistence;

import com.orders.domain.entity.SalesOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, UUID> {
    Page<SalesOrder> findAllByUserId(UUID userId, Pageable pageable);

    Optional<SalesOrder> findByIdAndUserId(UUID id, UUID userId);

    @Query(
            """
            SELECT COALESCE(SUM(i.units * i.unitPrice), 0)
            FROM SalesOrderItem i
            WHERE i.salesOrder.id = :id
            AND i.isActive IS TRUE
            """
    )
    BigDecimal getSalesOrderValue(@Param("id") UUID id);
}
