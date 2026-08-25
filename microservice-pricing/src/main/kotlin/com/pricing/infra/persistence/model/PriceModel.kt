package com.pricing.infra.persistence.model

import io.quarkus.hibernate.orm.panache.PanacheEntityBase
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity @Table(name = "price")
class PriceModel (
    @field:Column(name = "type_id")
    @field:NotNull
    var typeId: Int,

    @field:NotNull var value: BigDecimal,

    @field:ManyToOne
    @field:JoinColumn(name = "product_id")
    @field:NotNull
    var product: ProductModel,

    @field:NotNull
    var since: Instant = Instant.now(),

    var until: Instant? = null,

    @field:Column(name = "created_by") @field:NotNull
    var createdBy: UUID
): PanacheEntityBase() {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    lateinit var id: UUID

    @Column(name = "created_at") @NotNull
    var createdAt: Instant = Instant.now();

    @NotNull
    var active: Boolean = false

    @NotNull
    var deleted: Boolean = false
}