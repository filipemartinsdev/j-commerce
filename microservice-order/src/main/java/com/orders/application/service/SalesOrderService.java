package com.orders.application.service;

import com.orders.application.dto.PagedResponse;
import com.orders.application.dto.SalesOrderResponse;
import com.orders.application.dto.SalesOrderSummaryResponse;
import com.orders.application.exception.DeliveryAddressNotFoundException;
import com.orders.application.exception.SalesOrderNotFoundException;
import com.orders.application.message.CreateOrderMessage;
import static com.orders.application.message.CreateOrderMessage.OrderItem;

import com.orders.application.message.GeneratePaymentMessage;
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
    private final SalesOrderItemRepository salesOrderItemRepository;
    private final SalesOrderStatusRepository salesOrderStatusRepository;
    private final SalesOrderMapper salesOrderMapper;
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final ShippingStatusRepository shippingStatusRepository;
    private final ShippingRepository shippingRepository;
    private final MessageBrokerProducer messageBrokerProducer;

    public SalesOrderService(SalesOrderRepository salesOrderRepository, SalesOrderItemRepository salesOrderItemRepository, SalesOrderStatusRepository salesOrderStatusRepository, SalesOrderMapper salesOrderMapper, DeliveryAddressRepository deliveryAddressRepository, ShippingStatusRepository shippingStatusRepository, ShippingRepository shippingRepository, MessageBrokerProducer messageBrokerProducer) {
        this.salesOrderRepository = salesOrderRepository;
        this.salesOrderItemRepository = salesOrderItemRepository;
        this.salesOrderStatusRepository = salesOrderStatusRepository;
        this.salesOrderMapper = salesOrderMapper;
        this.deliveryAddressRepository = deliveryAddressRepository;
        this.shippingStatusRepository = shippingStatusRepository;
        this.shippingRepository = shippingRepository;
        this.messageBrokerProducer = messageBrokerProducer;
    }

    @Transactional
    public void createOrder(CreateOrderMessage message) {
        SalesOrder order = registerNewOrder(message.userId(), message.items());
//        registerSalesOrderItems(order, message.items());
        registerShipping(message.deliveryAddressId(), order);


        messageBrokerProducer.produceGeneratePayment(
                new GeneratePaymentMessage(order.getId(), message.userId(), getTotalAmount(order))
        );
    }

    private BigDecimal getTotalAmount(SalesOrder salesOrder){
        BigDecimal value = new BigDecimal(0);

        for (SalesOrderItem item : salesOrder.getItems()){
            value = value.add(item.getUnitPrice().multiply(new BigDecimal(item.getUnits())));
        }

        return value;
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

    private void registerShipping(UUID deliveryAddressId, SalesOrder salesOrder) {
        Shipping shipping = new Shipping();
        shipping.setSalesOrder(salesOrder);
        shipping.setDeliveryAddress(deliveryAddressRepository.findById(deliveryAddressId)
            .orElseThrow(() -> new DeliveryAddressNotFoundException("Delivery address not found with ID: " + deliveryAddressId))
        );
        shipping.setStatus(shippingStatusRepository.getReferenceById(
                ShippingStatus.Value.PENDING.getId()
        ));
        shippingRepository.save(shipping);
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
}
