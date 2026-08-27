package com.orders.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor
public class SalesOrderItemId implements Serializable {
    private SalesOrder salesOrder;
    private String sku;
}