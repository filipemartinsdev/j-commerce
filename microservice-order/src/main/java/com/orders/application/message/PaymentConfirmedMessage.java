package com.orders.application.message;

import java.util.UUID;

public record PaymentConfirmedMessage (
        UUID orderId
) {
}
