package com.orders.infra.persistence;

import com.orders.domain.entity.Shipping;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ShippingRepository extends JpaRepository<Shipping, UUID> {
    Page<Shipping> findAllBySalesOrderId(UUID salesOrderId, Pageable pageable);
}
