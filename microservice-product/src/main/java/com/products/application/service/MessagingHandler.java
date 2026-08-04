package com.products.application.service;

import com.products.application.dto.admin.CreateStockMovementRequest;
import com.products.application.message.OrderCheckedMessage;
import com.products.application.message.RefundItemsMessage;
import com.products.domain.entity.StockMovement;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MessagingHandler {
    private final StockService stockService;

    public MessagingHandler(StockService stockService) {
        this.stockService = stockService;
    }

    @Transactional
    public void refundItems(RefundItemsMessage message) {
        for (RefundItemsMessage.OrderItem item : message.items()){
            stockService.createMovement(new CreateStockMovementRequest(
                    item.SKU(),
                    Optional.of("Refunded order"),
                    StockMovement.Type.INBOUND.id,
                    StockMovement.Reason.REFUND.id,
                    item.units()
            ), null);
        }
    }

    @Transactional
    public void decreaseStock(OrderCheckedMessage message){
        for (OrderCheckedMessage.OrderItem item : message.items()){
            stockService.createMovement(new CreateStockMovementRequest(
                    item.SKU(),
                    Optional.of("Order checked"),
                    StockMovement.Type.OUTBOUND.id,
                    StockMovement.Reason.SALE.id,
                    item.units()
            ), null);
        }
    }
}
