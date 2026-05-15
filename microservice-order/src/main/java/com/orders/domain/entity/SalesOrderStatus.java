package com.orders.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity @Table(name = "sales_order_status")
@Data @NoArgsConstructor @AllArgsConstructor
public class SalesOrderStatus {
    @Id
    private Integer id;

    @NotBlank
    @Column(name = "name")
    private String name;

    public enum Value {
        PENDING(1),
        CONFIRMED(2),
        CANCELLED(3);

        @Getter
        private final int id;

        Value (int id) {
            this.id = id;
        }
    }
}