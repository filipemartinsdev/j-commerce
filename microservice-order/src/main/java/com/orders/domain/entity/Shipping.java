package com.orders.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "shipping")
@Data @NoArgsConstructor @AllArgsConstructor
public class Shipping {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "status_id")
    private ShippingStatus status;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id")
    private SalesOrder salesOrder;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "delivery_address_id")
    private DeliveryAddress deliveryAddress;

    @NotNull
    @Column(name = "expected_delivery_date")
    private Instant expectedDeliveryDate;

    @Column(name = "driver_id")
    private UUID driverId;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}