package com.orders.infra.persistence;

import com.orders.domain.entity.StorageAddress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface StorageAddressRepository extends JpaRepository<StorageAddress, UUID> {

    @Query("""
        SELECT s
        FROM StorageAddress s
        WHERE s.isActive IS TRUE
        ORDER BY s.createdAt
        LIMIT 1
        """
    )
    Optional<StorageAddress> findMainStorageAddress();

    @Query("""
        SELECT s
        FROM StorageAddress s
        WHERE s.isActive IS TRUE
        """
    )
    Page<StorageAddress> findAllActive(Pageable pageable);

    @Query("""
        SELECT s
        FROM StorageAddress s
        WHERE s.isActive IS TRUE
        AND s.id = :id
        """
    )
    Optional<StorageAddress> findActiveById(@Param("id") UUID id);
}
