package com.orders.application.service;

import com.orders.application.dto.PagedResponse;
import com.orders.application.dto.SalesOrderResponse;
import com.orders.application.dto.SalesOrderSummaryResponse;
import com.orders.application.exception.*;
import com.orders.application.message.*;

import static com.orders.application.message.CreateOrderMessage.OrderItem;

import com.orders.application.service.mapper.SalesOrderMapper;
import com.orders.domain.entity.*;
import com.orders.infra.persistence.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class SalesOrderService {
    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderStatusRepository salesOrderStatusRepository;
    private final SalesOrderMapper salesOrderMapper;
    private final ShippingStatusRepository shippingStatusRepository;
    private final MessageBrokerProducer messageBrokerProducer;

    public SalesOrderService(SalesOrderRepository salesOrderRepository, SalesOrderStatusRepository salesOrderStatusRepository, SalesOrderMapper salesOrderMapper, ShippingStatusRepository shippingStatusRepository, MessageBrokerProducer messageBrokerProducer) {
        this.salesOrderRepository = salesOrderRepository;
        this.salesOrderStatusRepository = salesOrderStatusRepository;
        this.salesOrderMapper = salesOrderMapper;
        this.shippingStatusRepository = shippingStatusRepository;
        this.messageBrokerProducer = messageBrokerProducer;
    }

    //    TODO: update unit tests to include new message producing
    @Transactional
    public void createOrder(CreateOrderMessage message) {
        if(message.items().isEmpty())
            throw new CantCreateSalesOrderException("Cant create order because items is empty");

        SalesOrder order = registerNewOrder(message.userId(), message.items());
//        registerShipping(message.deliveryAddressId(), order);

        messageBrokerProducer.produceGeneratePayment(
                new GeneratePaymentMessage(order.getId(), message.userId(), getTotalAmount(order))
        );

        messageBrokerProducer.produceCreateShipping(
                new CreateShippingMessage(order.getId(), message.deliveryAddressId())
        );
    }

    private SalesOrder registerNewOrder(UUID userId, List<OrderItem> items) {
        var order = new SalesOrder();
        order.setUserId(userId);
        order.setStatus(
                salesOrderStatusRepository.getReferenceById(SalesOrderStatus.Value.PENDING.getId())
        );

        setSalesOrderItems(order, items);

        return salesOrderRepository.save(order);
    }

    private BigDecimal getTotalAmount(SalesOrder salesOrder){
        return salesOrder.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(new BigDecimal(item.getUnits())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void setSalesOrderItems(SalesOrder salesOrder, List<OrderItem> items) {
        List<SalesOrderItem> salesOrderItems = items.stream()
                .map(item -> {
                    var salesOrderItem = new SalesOrderItem();
                    salesOrderItem.setSalesOrder(salesOrder);
                    salesOrderItem.setProductSkuId(item.getProductSKUId());
                    salesOrderItem.setUnits(item.getUnits());
                    salesOrderItem.setUnitPrice(item.getUnitPrice());
                    salesOrderItem.setProductSkuName(item.getName());
                    return salesOrderItem;
                })
                .toList();
        salesOrder.setItems(salesOrderItems);
    }

    public PagedResponse<SalesOrderResponse> getAllByUserId(UUID userId, Pageable pageable) {
        Page<SalesOrder> page = salesOrderRepository.findAllByUserId(userId, pageable);

        return PagedResponse.<SalesOrderResponse>builder()
                .page(page.getNumber())
                .size(page.getSize())
                .isLast(page.isLast())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .content(page.getContent().stream()
                        .map(salesOrderMapper::toResponse)
                        .toList()
                )
                .build();
    }

    public SalesOrderSummaryResponse getSummaryById(UUID id, UUID userId) {
        return salesOrderMapper.toSummaryResponse(
                salesOrderRepository.findByIdAndUserId(id, userId)
                        .orElseThrow(() -> new SalesOrderNotFoundException("Sales order not found with ID: "+id))
        );
    }

    @Transactional
    public void handleSalesOrderPaymentTimeout(HandlePaymentTimeoutMessage message) {
        SalesOrder order = salesOrderRepository.findById(message.orderId())
                .orElseThrow(() -> new SalesOrderNotFoundException("Sales order not found with ID: "+message.orderId()));

        if (order.getStatus().getId().equals(SalesOrderStatus.Value.PENDING.getId())) {
            cancelOrder(order);
        }
    }

    private void cancelOrder(SalesOrder order) {
        order.setStatus(salesOrderStatusRepository.getReferenceById(SalesOrderStatus.Value.CANCELLED.getId()));

        salesOrderRepository.save(order);

        publishOrderCancelledMessage(order);
    }

    private void publishOrderCancelledMessage(SalesOrder order){
        SalesOrderCancelledMessage message = new SalesOrderCancelledMessage(
                order.getId(),
                order.getUserId(),
                order.getItems().stream()
                        .map(salesOrderItem ->
                                new SalesOrderCancelledMessage.OrderItem(salesOrderItem.getProductSkuId(), salesOrderItem.getUnits())
                        )
                        .toList(),
                getTotalAmount(order)
        );

        messageBrokerProducer.produceOrderCancelled(message);
    }


    public void confirmOrderPayment(PaymentConfirmedMessage message) {
        SalesOrder order = salesOrderRepository.findById(message.orderId())
                .orElseThrow(() -> new SalesOrderNotFoundException("Sales order not found with ID: "+message.orderId()));

        order.setStatus(salesOrderStatusRepository.getReferenceById(SalesOrderStatus.Value.CONFIRMED.getId()));
        salesOrderRepository.save(order);
    }

    public void requestToCancelOrder(UUID id, UUID userId) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new SalesOrderNotFoundException("Sales order not found with ID: "+id));

        if (canUserCancelOrder(userId, order)){
            cancelOrder(order);
        }

        else {
            throw new CantCancelSalesOrderException("Can't cancel this order");
        }
    }

    private boolean canUserCancelOrder(UUID userId, SalesOrder order) {
        return (isOrderStatusPending(order) && isUserOwnerOfOrder(userId, order));
    }

    private boolean isOrderStatusPending(SalesOrder order) {
        return order.getStatus().getId().equals(SalesOrderStatus.Value.PENDING.getId());
    }

    private boolean isUserOwnerOfOrder(UUID userId, SalesOrder order) {
        return order.getUserId().equals(userId);
    }
}
