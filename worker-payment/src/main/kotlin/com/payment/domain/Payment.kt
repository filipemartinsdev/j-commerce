package com.payment.domain

import io.quarkus.hibernate.orm.panache.PanacheEntityBase
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.UpdateTimestamp
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity @Table(name = "payment")
class Payment: PanacheEntityBase() {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    lateinit var id: UUID

    @Column(name = "provider_id")
    lateinit var providerId: UUID

    @Column(name = "user_id")
    lateinit var userId: UUID

    @Column(name = "sales_order_id")
    lateinit var salesOrderId: UUID

    @ManyToOne
    @JoinColumn(name = "status_id")
    lateinit var status: PaymentStatus

    lateinit var amount: BigDecimal

    @Column(name = "created_at")
    var createdAt: Instant = Instant.now()

    @Column(name = "updated_at")
    @UpdateTimestamp
    lateinit var updatedAt: Instant
}