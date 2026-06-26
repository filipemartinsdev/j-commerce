package com.orders.infra.persistence;

import com.orders.domain.entity.SalesOrderItem;
import com.orders.domain.entity.SalesOrderItemId;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesOrderItemRepository extends JpaRepository<SalesOrderItem, SalesOrderItemId> {
}
