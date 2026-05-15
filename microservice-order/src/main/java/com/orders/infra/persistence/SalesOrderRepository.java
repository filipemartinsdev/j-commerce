package com.orders.infra.persistence;

import com.orders.domain.entity.SalesOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, UUID> {
    Page<SalesOrder> findAllByUserId(UUID userId, Pageable pageable);

    Optional<SalesOrder> findByIdAndUserId(UUID id, UUID userId);
}
