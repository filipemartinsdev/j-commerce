package com.payment.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity @Table(name = "payment_status")
class PaymentStatus {
    companion object {
        enum class Value (val id: Int) {
            PENDING(1),
            PAID(2),
            REFUNDED(3);
        }
    }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0

    lateinit var name: String
}
