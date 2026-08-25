package com.products.application.message;

import java.io.Serializable;

public record SKUDeletedMessage(
        String SKU
) implements Serializable {
}
