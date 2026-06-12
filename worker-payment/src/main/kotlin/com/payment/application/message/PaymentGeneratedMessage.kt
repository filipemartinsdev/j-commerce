package com.payment.application.message

import java.math.BigDecimal
import java.util.*

data class PaymentGeneratedMessage(
    val paymentId: UUID,
    val orderId: UUID,
    val userId: UUID,
    val value: BigDecimal
)
