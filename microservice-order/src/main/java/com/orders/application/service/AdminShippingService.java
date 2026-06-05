package com.orders.application.service;

import com.orders.application.dto.ShippingRequest;
import com.orders.application.dto.ShippingResponse;
import com.orders.application.exception.*;
import com.orders.application.message.CreateShippingMessage;
import com.orders.application.service.mapper.ShippingMapper;
import com.orders.domain.entity.*;
import com.orders.infra.persistence.DeliveryAddressRepository;
import com.orders.infra.persistence.SalesOrderRepository;
import com.orders.infra.persistence.ShippingRepository;
import com.orders.infra.persistence.ShippingStatusRepository;
import io.github.responsekit.core.PagedResponse;
import io.github.responsekit.spring.PagedResponseFactory;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AdminShippingService {
    private final ShippingRepository shippingRepository;
    private final ShippingStatusRepository shippingStatusRepository;
    private final ShippingMapper shippingMapper;
    private final SalesOrderRepository salesOrderRepository;
    private final DeliveryAddressRepository deliveryAddressRepository;
    private final DeliveryDateCalculator deliveryDateCalculator;

    public AdminShippingService(ShippingRepository shippingRepository, ShippingStatusRepository shippingStatusRepository, ShippingMapper shippingMapper, SalesOrderRepository salesOrderRepository, DeliveryAddressRepository deliveryAddressRepository, DeliveryDateCalculator deliveryDateCalculator) {
        this.shippingRepository = shippingRepository;
        this.shippingStatusRepository = shippingStatusRepository;
        this.shippingMapper = shippingMapper;
        this.salesOrderRepository = salesOrderRepository;
        this.deliveryAddressRepository = deliveryAddressRepository;
        this.deliveryDateCalculator = deliveryDateCalculator;
    }

    public void createShippingFromMessage(CreateShippingMessage message) {
        DeliveryAddress deliveryAddress = deliveryAddressRepository.findById(message.deliveryAddressId())
                .orElseThrow(() -> new DeliveryAddressNotFoundException("Delivery address not found with ID: " + message.deliveryAddressId()));

        Shipping shipping = new Shipping();
        shipping.setSalesOrder(salesOrderRepository.getReferenceById(message.salesOrderId()));
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

//    TODO: unit tests
    public void createShipping(ShippingRequest request){

    }

    public void finishShipping(UUID shippingId){
        Shipping shipping = shippingRepository.findById(shippingId)
                .orElseThrow(() -> new ShippingNotFoundException("Shipping not found with ID: " + shippingId));

        if(!isShippingInTransit(shipping))
            throw new CantCheckOutShippingException("Shipping is not in transit");

        shipping.setStatus(shippingStatusRepository.getReferenceById(ShippingStatus.Value.DELIVERED.getId()));

        shippingRepository.save(shipping);
    }

    private boolean isShippingInTransit(Shipping shipping) {
        return shipping.getStatus().getId().equals(ShippingStatus.Value.IN_TRANSIT.getId());
    }

    public void cancelShipmentsBySalesOrderId(UUID salesOrderId){
        SalesOrder salesOrder = salesOrderRepository.findById(salesOrderId)
                .orElseThrow(() -> new SalesOrderNotFoundException("SalesOrder not found by ID: " + salesOrderId));

        for (Shipping shipping : salesOrder.getShipments())
            shipping.setStatus(shippingStatusRepository.getReferenceById(ShippingStatus.Value.CANCELLED.getId()));

        salesOrderRepository.save(salesOrder);
    }

    @Transactional
    public void cancelShipping(UUID shippingId){
        Shipping shipping = shippingRepository.findById(shippingId)
                .orElseThrow(() -> new ShippingNotFoundException("Shipping not found with ID: " + shippingId));

        if (isShippingCancelled(shipping))
            throw new CantCancelShippingException("Shipping is cancelled");

        if (isShippingDelivered(shipping))
            throw new CantCancelShippingException("Shipping is delivered");

        shipping.setStatus(shippingStatusRepository.getReferenceById(ShippingStatus.Value.CANCELLED.getId()));
        shippingRepository.save(shipping);
    }

    private boolean isShippingDelivered(Shipping shipping){
        return shipping.getStatus().getId().equals(ShippingStatus.Value.DELIVERED.getId());
    }

    private boolean isShippingCancelled(Shipping shipping){
        return shipping.getStatus().getId().equals(ShippingStatus.Value.CANCELLED.getId());
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

    public void dispatchShipping(UUID id) {
        Shipping shipping = shippingRepository.findById(id)
                .orElseThrow(() -> new ShippingNotFoundException("Shipping not found by ID: " + id));

        if(isShippingDispatched(shipping) || !isShippingPending(shipping))
            throw new CantDispatchShippingException("Shipping was already been dispatched");

        if(!isOrderConfirmed(shipping.getSalesOrder()))
            throw new CantDispatchShippingException("Order hasn't been confirmed yet");

        updateShippingStatusToDispatched(shipping);
    }

//    TODO: create startShipping message to notify the event
    public void startShipping(UUID id, UUID driverId) {
        Shipping shipping = shippingRepository.findById(id)
                .orElseThrow(() -> new ShippingNotFoundException("Shipping not found by ID: " + id));

        if (isShippingInTransit(shipping) || hasDriver(shipping))
            throw new CantCheckInShippingException("Shipping is already in transit");

        if (isShippingCancelled(shipping))
            throw new CantCheckInShippingException("Shipping is cancelled");

        if (isShippingDelivered(shipping))
            throw new CantCheckInShippingException("Shipping is delivered");

        if (!isShippingDispatched(shipping))
            throw new CantCheckInShippingException("Shipping wasn't been dispatched yet");

        shipping.setDriverId(driverId);
        shipping.setStatus(shippingStatusRepository.getReferenceById(ShippingStatus.Value.IN_TRANSIT.getId()));
        shippingRepository.save(shipping);
    }

    private boolean isShippingPending(Shipping shipping) {
        return shipping.getStatus().getId().equals(ShippingStatus.Value.PENDING.getId());
    }

    private boolean hasDriver(Shipping shipping) {
        return shipping.getDriverId() != null;
    }

    private boolean isShippingDispatched(Shipping shipping){
        return shipping.getStatus().getId().equals(ShippingStatus.Value.DISPATCHED.getId());
    }

    private boolean isOrderConfirmed(SalesOrder salesOrder) {
        return salesOrder.getStatus().getId().equals(SalesOrderStatus.Value.CONFIRMED.getId());
    }

    private void updateShippingStatusToDispatched(Shipping shipping) {
        shipping.setStatus(
                shippingStatusRepository.getReferenceById(ShippingStatus.Value.DISPATCHED.getId())
        );
        shippingRepository.save(shipping);
    }
}
