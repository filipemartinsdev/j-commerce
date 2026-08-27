package com.orders.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.time.Instant;

@Entity @Table(name = "sales_order_item")
@Data @NoArgsConstructor @AllArgsConstructor
@IdClass(SalesOrderItemId.class)
public class SalesOrderItem {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id")
    private SalesOrder salesOrder;

    @Id
    @Length(max = 50)
    @Column(name = "sku")
    private String sku;

    @NotBlank
    @Length(max = 255)
    @Column(name = "name")
    private String name;

    @NotNull
    @Column(name = "unit_price")
    private BigDecimal unitPrice;

    @NotNull
    @Column(name = "units")
    private Integer units;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @NotNull
    @Column(name = "is_active")
    private Boolean isActive = true;
}