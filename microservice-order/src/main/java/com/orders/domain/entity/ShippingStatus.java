package com.orders.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.EnumMap;
import java.util.EnumSet;

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
        DISPATCHED(2),
        IN_TRANSIT(3),
        DELIVERED(4),
        CANCELLED(5);

        @Getter
        private final int id;

        Value(int id) {
            this.id = id;
        }

        public static Value byId(int id){
            return switch (id){
                case 1 -> Value.PENDING;
                case 2 -> Value.DISPATCHED;
                case 3 -> Value.IN_TRANSIT;
                case 4 -> Value.DELIVERED;
                case 5 -> Value.CANCELLED;
                default -> throw new IllegalArgumentException("No enum constant for id: " + id);
            };
        }
    }

    public static EnumMap<Value, EnumSet<Value>> TRANSITIONS = new EnumMap<>(Value.class);

    static {
        TRANSITIONS.put(Value.PENDING, EnumSet.of(
                Value.DISPATCHED, Value.CANCELLED
        ));

        TRANSITIONS.put(Value.DISPATCHED, EnumSet.of(
                Value.IN_TRANSIT, Value.CANCELLED
        ));

        TRANSITIONS.put(Value.IN_TRANSIT, EnumSet.of(
                Value.DELIVERED, Value.CANCELLED
        ));

        TRANSITIONS.put(Value.DELIVERED, EnumSet.noneOf(Value.class));

        TRANSITIONS.put(Value.CANCELLED, EnumSet.noneOf(Value.class));
    }

    public static boolean canTransition(Value from, Value to){
        return TRANSITIONS.get(from).contains(to);
    }
}