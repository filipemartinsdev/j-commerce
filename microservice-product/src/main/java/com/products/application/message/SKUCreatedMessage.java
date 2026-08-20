package com.products.application.message;

import java.io.Serializable;

public record SKUCreatedMessage(
        String SKU
) implements Serializable {
}
