package com.payment.application.message

import java.math.BigDecimal
import java.util.*


data class GeneratePaymentMessage(
    val orderId: UUID,
    val userId: UUID,
    val totalAmount: BigDecimal
)
