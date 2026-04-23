package com.orders.infra.persistence;

import com.orders.domain.entity.Shipping;
import com.orders.domain.entity.ShippingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ShippingStatusRepository extends JpaRepository<ShippingStatus, Integer> {
}
