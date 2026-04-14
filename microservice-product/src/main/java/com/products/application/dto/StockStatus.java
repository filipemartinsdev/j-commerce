package com.products.application.dto;

public enum StockStatus {
    IN_STOCK,
    LAST_STOCK,
    OUT_STOCK;

    public static StockStatus fromStockCount(int count){
        if (count <= 0){
            return OUT_STOCK;
        }

        if (count < 10){
            return LAST_STOCK;
        }

        return IN_STOCK;
    }
}
