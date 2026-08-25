package com.products.domain.entity;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Document(collection = "stockMovements")
@Data @NoArgsConstructor @AllArgsConstructor
public class StockMovement {
    @Id
    private String id;

    @NotEmpty @Indexed
    private String SKU;

    @NotNull @Positive
    private Integer units;

    @NotNull
    private MovementType type;

    @NotNull
    private MovementReason reason;

    private String description;

    @NotNull
    private Instant createdAt = Instant.now();

    @NotNull
    private UUID createdBy;


    @Data @AllArgsConstructor @NoArgsConstructor
    public static class MovementType {
        @NotNull @Indexed
        private Integer id;

        @NotEmpty
        private String name;
    }

    @Data @AllArgsConstructor @NoArgsConstructor
    public static class MovementReason {
        @NotNull @Indexed
        private Integer id;

        @NotEmpty
        private String name;
    }

    public static enum Type {
        INBOUND(1),
        OUTBOUND(2);

        public final int id;

        Type(int id){
            this.id = id;
        }

        public static Optional<Type> getById(int id){
            for (Type type : Type.values())
                if (type.id == id) return Optional.of(type);

            return Optional.empty();
        }
    }

    public static enum Reason {
        ENTRY(1),
        SALE(2),
        REFUND(3),
        ADJUST(4),
        DAMAGE(5),
        OTHER(6);

        public final int id;

        Reason(int id) {
            this.id = id;
        }

        public static Optional<Reason> getById(int id){
            for (Reason type : Reason.values())
                if (type.id == id) return Optional.of(type);
            return Optional.empty();
        }
    }
}
