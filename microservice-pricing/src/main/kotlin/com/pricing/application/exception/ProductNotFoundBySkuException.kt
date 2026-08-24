package com.pricing.application.exception

class ProductNotFoundBySkuException(sku: String) : RuntimeException("Product not found by SKU: $sku") {
}
