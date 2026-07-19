package com.payment.application.message

import java.io.Serializable
import java.math.BigDecimal
import java.util.UUID

data class PaymentTimeoutMessage (
    val paymentId: UUID,
    val orderId: UUID,
    val userId: UUID,
    val amount: BigDecimal,
) : Serializable
