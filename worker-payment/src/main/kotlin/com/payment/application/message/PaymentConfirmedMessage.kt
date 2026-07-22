package com.payment.application.message

import java.io.Serializable
import java.math.BigDecimal
import java.util.*

data class PaymentConfirmedMessage(
    val paymentId: UUID,
    val orderId: UUID,
    val userId: UUID,
    val amount: BigDecimal
) : Serializable
