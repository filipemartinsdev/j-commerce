package com.payment.application.message

import java.io.Serializable
import java.math.BigDecimal
import java.util.UUID

data class GeneratePaymentMessage(
    val salesOrderId: UUID,
    val userId: UUID,
    val deliveryAddressId: UUID,
    val totalAmount: BigDecimal
) : Serializable
