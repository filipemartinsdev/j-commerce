package com.pricing.application.exception

import jakarta.ws.rs.NotFoundException

class SkuNotFoundException (
    message: String
): NotFoundException(message)