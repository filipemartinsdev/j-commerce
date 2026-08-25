package com.pricing.application.gateway

import com.pricing.application.dto.PriceCheckedMessage
import com.pricing.application.dto.PriceUpdatedMessage

interface MessageProducerGateway {
    fun producePriceChecked(message: PriceCheckedMessage)

    fun producePriceUpdated(message: PriceUpdatedMessage)
}
