package com.orders.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Set;

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

        public static Value byId(int id){
            return switch (id){
                case 1 -> PENDING;
                case 2 -> CONFIRMED;
                case 3 -> CANCELLED;
                default -> throw new IllegalArgumentException("No enum constant for id: " + id);
            };
        }
    }

    public static EnumMap<Value, Set<Value>> TRANSITIONS = new EnumMap<>(Value.class);

    static {
        TRANSITIONS.put(Value.PENDING, EnumSet.of(
                Value.CANCELLED, Value.CONFIRMED
        ));

        TRANSITIONS.put(Value.CONFIRMED, EnumSet.noneOf(Value.class));

        TRANSITIONS.put(Value.CANCELLED, EnumSet.noneOf(Value.class));
    }

    public static boolean canTransition(Value from, Value to){
        return TRANSITIONS.get(from).contains(to);
    }
}