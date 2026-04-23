package com.orders.application.service;

import com.orders.application.dto.*;
import com.orders.application.exception.DeliveryAddressNotFoundException;
import com.orders.application.exception.SalesOrderNotFoundException;
import com.orders.application.service.mapper.SalesOrderMapper;
import com.orders.domain.entity.*;
import com.orders.infra.persistence.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public SalesOrderService(SalesOrderRepository salesOrderRepository, SalesOrderItemRepository salesOrderItemRepository, SalesOrderStatusRepository salesOrderStatusRepository, SalesOrderMapper salesOrderMapper, DeliveryAddressRepository deliveryAddressRepository, ShippingStatusRepository shippingStatusRepository, ShippingRepository shippingRepository) {
        this.salesOrderRepository = salesOrderRepository;
        this.salesOrderItemRepository = salesOrderItemRepository;
        this.salesOrderStatusRepository = salesOrderStatusRepository;
        this.salesOrderMapper = salesOrderMapper;
        this.deliveryAddressRepository = deliveryAddressRepository;
        this.shippingStatusRepository = shippingStatusRepository;
        this.shippingRepository = shippingRepository;
    }

    @Transactional
    public void confirmShoppingCart(ShoppingCartConfirmation dto) {
        SalesOrder order = registerNewOrder(dto.getUserId());
        registerSalesOrderItems(order, dto.getItems());
        registerShipping(dto.getDeliveryAddressId(), order);
    }

    private SalesOrder registerNewOrder(UUID userId) {
        var newSalesOrder = new SalesOrder();
        newSalesOrder.setUserId(userId);
        newSalesOrder.setStatus(
                salesOrderStatusRepository.getReferenceById(SalesOrderStatus.Value.PENDING.getId())
        );
        return salesOrderRepository.save(newSalesOrder);
    }

    private void registerSalesOrderItems(SalesOrder salesOrder, List<ShoppingCartConfirmationItem> shoppingCartItems) {
        List<SalesOrderItem> salesOrderItems = shoppingCartItems.stream()
                .map(shoppingCartItem -> {
                    var salesOrderItem = new SalesOrderItem();
                    salesOrderItem.setSalesOrder(salesOrder);
                    salesOrderItem.setProductSkuId(shoppingCartItem.getProductSKUId());
                    salesOrderItem.setUnits(shoppingCartItem.getUnits());
                    salesOrderItem.setUnitPrice(shoppingCartItem.getUnitPrice());
                    salesOrderItem.setProductSkuName(shoppingCartItem.getName());
                    return salesOrderItem;
                })
                .toList();
        salesOrderItemRepository.saveAll(salesOrderItems);
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
