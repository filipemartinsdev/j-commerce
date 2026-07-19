package com.orders.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;
import java.util.List;

@Entity @Table(name = "sales_order")
@Data @NoArgsConstructor @AllArgsConstructor
public class SalesOrder {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name = "user_id")
    private UUID userId;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "status_id")
    private SalesOrderStatus status;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @OneToMany(
            fetch = FetchType.LAZY,
            mappedBy = "salesOrder",
            cascade = CascadeType.ALL
    )
    @SQLRestriction("is_active IS TRUE")
    private List<SalesOrderItem> items = new ArrayList<>();

    @OneToMany(
            fetch = FetchType.LAZY,
            mappedBy = "salesOrder",
            cascade = CascadeType.ALL
    )
    private List<Shipping> shipments = new ArrayList<>();
}