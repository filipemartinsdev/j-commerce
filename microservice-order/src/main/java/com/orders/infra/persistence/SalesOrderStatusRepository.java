package com.orders.infra.persistence;

import com.orders.domain.entity.SalesOrder;
import com.orders.domain.entity.SalesOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SalesOrderStatusRepository extends JpaRepository<SalesOrderStatus, Integer> {
}
