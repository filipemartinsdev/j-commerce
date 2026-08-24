package com.pricing.infra.persistence.model

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.SQLOrder
import org.hibernate.annotations.SQLRestriction
import java.util.UUID

@Entity @Table(name = "product")
class ProductModel(
    @field:NotEmpty
    var sku: String
) {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    lateinit var id: UUID

    @NotNull
    @OneToMany(mappedBy = "product_id")
    @SQLRestriction("deleted = FALSE AND active = TRUE")
    @SQLOrder("created_at ASC")
    var activePrices: MutableList<PriceModel> = mutableListOf();

    @NotNull
    var deleted: Boolean = false
}
