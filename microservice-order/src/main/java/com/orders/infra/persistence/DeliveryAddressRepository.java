package com.orders.infra.persistence;

import com.orders.domain.entity.DeliveryAddress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, UUID> {
    @Query(
            """
            SELECT a
            FROM DeliveryAddress a
            WHERE a.isActive IS TRUE
            AND a.userId = :userId
            """
    )
    Page<DeliveryAddress> findAllActiveByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query(
            """
            SELECT a
            FROM DeliveryAddress a
            WHERE a.id = :id
            AND a.userId = :userId
            AND a.isActive IS TRUE
            """
    )
    Optional<DeliveryAddress> findActiveByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);
}
