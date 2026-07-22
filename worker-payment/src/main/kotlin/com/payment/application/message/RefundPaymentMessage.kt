package com.payment.application.message

import java.io.Serializable
import java.math.BigDecimal
import java.util.UUID

data class RefundPaymentMessage (
    val salesOrderId: UUID,
    val userId: UUID,
    val items: List<OrderItem>,
    val totalAmount: BigDecimal

) : Serializable {
    data class OrderItem(
        val productSkuId: UUID,
        val units: Int
    ) : Serializable
}
