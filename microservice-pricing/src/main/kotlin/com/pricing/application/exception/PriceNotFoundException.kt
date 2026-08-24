package com.pricing.application.exception

import jakarta.ws.rs.NotFoundException

class PriceNotFoundException (
    message: String
): NotFoundException(message)