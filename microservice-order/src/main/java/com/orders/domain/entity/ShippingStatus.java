package com.orders.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity @Table(name = "shipping_status")
@Data @NoArgsConstructor @AllArgsConstructor
public class ShippingStatus {
    @Id
    private Integer id;

    @NotBlank
    @Column(name = "name")
    private String name;

    public enum Value {
        PENDING(1),
        IN_TRANSIT(2),
        IN_DISTRIBUTION_CENTER(3),
        DELIVERED(4),
        CANCELLED(5);

        @Getter
        private final int id;

        Value(int id) {
            this.id = id;
        }
    }
}