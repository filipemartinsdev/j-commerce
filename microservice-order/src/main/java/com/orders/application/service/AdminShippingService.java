package com.orders.application.service;

import com.orders.application.dto.ShippingResponse;
import com.orders.application.exception.*;
import com.orders.application.message.SalesOrderCreatedMessage;
import com.orders.application.message.SalesOrderDispatchedMessage;
import com.orders.application.service.mapper.ShippingMapper;
import com.orders.domain.entity.*;
import com.orders.infra.messaging.MessageBrokerProducer;
import com.orders.infra.persistence.DeliveryAddressRepository;
import com.orders.infra.persistence.SalesOrderRepository;
import com.orders.infra.persistence.ShippingRepository;
import com.orders.infra.persistence.ShippingStatusRepository;
import io.github.responsekit.core.PagedResponse;
import io.github.responsekit.spring.PagedResponseFactory;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AdminShippingService {
    private final ShippingRepository shippingRepository;
    private final ShippingStatusRepository shippingStatusRepository;
    private final ShippingMapper shippingMapper;
    private final SalesOrderRepository salesOrderRepository;
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final DeliveryDateCalculator deliveryDateCalculator;
    private final MessageBrokerProducer messageBrokerProducer;

    public AdminShippingService(ShippingRepository shippingRepository, ShippingStatusRepository shippingStatusRepository, ShippingMapper shippingMapper, SalesOrderRepository salesOrderRepository, DeliveryAddressRepository deliveryAddressRepository, DeliveryDateCalculator deliveryDateCalculator, MessageBrokerProducer messageBrokerProducer) {
        this.shippingRepository = shippingRepository;
        this.shippingStatusRepository = shippingStatusRepository;
        this.shippingMapper = shippingMapper;
        this.salesOrderRepository = salesOrderRepository;
        this.deliveryAddressRepository = deliveryAddressRepository;
        this.deliveryDateCalculator = deliveryDateCalculator;
        this.messageBrokerProducer = messageBrokerProducer;
    }

    public void createShipping(SalesOrderCreatedMessage message) {
        DeliveryAddress deliveryAddress = deliveryAddressRepository.findById(message.deliveryAddressId())
                .orElseThrow(() -> new DeliveryAddressNotFoundException("Delivery address not found with ID: " + message.deliveryAddressId()));

        Shipping shipping = new Shipping();
        shipping.setSalesOrder(salesOrderRepository.getReferenceById(message.id()));
        shipping.setDeliveryAddress(deliveryAddress);
        shipping.setStatus(shippingStatusRepository.getReferenceById(
                ShippingStatus.Value.PENDING.getId()
        ));
        shipping.setExpectedDeliveryDate(
                deliveryDateCalculator.getDeliveryDate(
                        deliveryAddress.getLatitude(),
                        deliveryAddress.getLongitude()
                )
        );

        shippingRepository.save(shipping);
    }

    public void finishShipping(UUID shippingId){
        Shipping shipping = shippingRepository.findById(shippingId)
                .orElseThrow(() -> new ShippingNotFoundException("Shipping not found with ID: " + shippingId));

        var fromStatus = ShippingStatus.Value.byId(shipping.getStatus().getId());
        var toStatus = ShippingStatus.Value.DELIVERED;

        if (!ShippingStatus.canTransition(fromStatus, toStatus))
            throw new CantTransitionShippingStatusException("Can't transition shipping status from " + fromStatus + " to " + toStatus);

        shipping.setStatus(shippingStatusRepository.getReferenceById(ShippingStatus.Value.DELIVERED.getId()));

        shippingRepository.save(shipping);
    }

    public void cancelShipmentsBySalesOrderId(UUID salesOrderId){
        SalesOrder salesOrder = salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new SalesOrderNotFoundException("SalesOrder not found by ID: " + salesOrderId));

        SalesOrderStatus.Value orderStatus = SalesOrderStatus.Value.byId(salesOrder.getStatus().getId());

        if (!orderStatus.equals(SalesOrderStatus.Value.CANCELLED))
            throw new CantTransitionShippingStatusException("Can't cancel shipping from a non canceled order");

        for (Shipping shipping : salesOrder.getShipments())
            shipping.setStatus(shippingStatusRepository.getReferenceById(ShippingStatus.Value.CANCELLED.getId()));

        salesOrderRepository.save(salesOrder);
    }

    @Transactional
    public void cancelShipping(UUID shippingId){
        Shipping shipping = shippingRepository.findById(shippingId)
                .orElseThrow(() -> new ShippingNotFoundException("Shipping not found with ID: " + shippingId));

        var fromStatus = ShippingStatus.Value.byId(shipping.getStatus().getId());
        var toStatus = ShippingStatus.Value.CANCELLED;

        if (!ShippingStatus.canTransition(fromStatus, toStatus))
            throw new CantTransitionShippingStatusException("Can't transition shipping status from " + fromStatus + " to " + toStatus);

        shipping.setStatus(shippingStatusRepository.getReferenceById(ShippingStatus.Value.CANCELLED.getId()));
        shippingRepository.save(shipping);
    }

    public PagedResponse<ShippingResponse> getAll(Pageable pageable) {
        return PagedResponseFactory.fromPage(
                shippingRepository.findAll(pageable),
                shippingMapper::toResponse
        );
    }

    public PagedResponse<ShippingResponse> getAllBySalesOrderId(UUID salesOrderId, Pageable pageable) {
        return PagedResponseFactory.fromPage(
                shippingRepository.findAllBySalesOrderId(salesOrderId, pageable),
                shippingMapper::toResponse
        );
    }

    @Transactional
    public void dispatchShipping(UUID id) {
        Shipping shipping = shippingRepository.findById(id)
                .orElseThrow(() -> new ShippingNotFoundException("Shipping not found by ID: " + id));

        if (!shipping.getSalesOrder().getStatus().getId().equals(SalesOrderStatus.Value.CONFIRMED.getId()))
            throw new CantDispatchShippingException("Can't dispatch shipping status from a non confirmed order");

        var fromStatus = ShippingStatus.Value.byId(shipping.getStatus().getId());
        var toStatus = ShippingStatus.Value.DISPATCHED;

        if (!ShippingStatus.canTransition(fromStatus, toStatus))
            throw new CantTransitionShippingStatusException("Can't transition shipping status from " + fromStatus + " to " + toStatus);

        shipping.setStatus(
                shippingStatusRepository.getReferenceById(ShippingStatus.Value.DISPATCHED.getId())
        );
        shippingRepository.save(shipping);

        messageBrokerProducer.produceOrderDispatched(
                new SalesOrderDispatchedMessage(
                        shipping.getSalesOrder().getId(),
                        shipping.getSalesOrder().getUserId(),
                        shipping.getDeliveryAddress().getId(),
                        salesOrderRepository.getSalesOrderValue(shipping.getSalesOrder().getId())
                )
        );
    }

    public void startShipping(UUID shippingId, UUID driverId) {
        Shipping shipping = shippingRepository.findById(shippingId)
                .orElseThrow(() -> new ShippingNotFoundException("Shipping not found by ID: " + shippingId));

        var fromStatus = ShippingStatus.Value.byId(shipping.getStatus().getId());
        var toStatus = ShippingStatus.Value.IN_TRANSIT;

        if (!ShippingStatus.canTransition(fromStatus, toStatus))
            throw  new CantTransitionShippingStatusException("Can't transition shipping status from " + fromStatus + " to " + toStatus);

        shipping.setDriverId(driverId);
        shipping.setStatus(shippingStatusRepository.getReferenceById(ShippingStatus.Value.IN_TRANSIT.getId()));
        shippingRepository.save(shipping);
    }

    public ShippingResponse getById(UUID id) {
        return shippingMapper.toResponse(
                shippingRepository.findById(id)
                        .orElseThrow(() -> new ShippingNotFoundException("Shipping not found by ID: " + id))
        );
    }
}
