package com.products.application.service;

import com.products.application.dto.PagedResponse;
import com.products.application.dto.catalogue.ConfirmShoppingCartRequest;
import com.products.application.dto.catalogue.CreateShoppingCartItemRequest;
import com.products.application.dto.catalogue.ShoppingCartItemResponse;
import com.products.application.exception.*;
import com.products.application.message.CreateOrderMessage;
import com.products.application.service.mapper.ShoppingCartItemMapper;
import com.products.domain.entity.ProductSKU;
import com.products.domain.entity.ShoppingCartItem;
import com.products.domain.entity.ShoppingCartItemProductSKUSummary;
import com.products.infra.persistence.ProductSKURepository;
import com.products.infra.persistence.ShoppingCartItemProductSKUSummaryRepository;
import com.products.infra.persistence.ShoppingCartItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ShoppingCartService {
    private final ShoppingCartItemRepository shoppingCartItemRepository;
    private final ShoppingCartItemMapper shoppingCartItemProductSKUMapper;
    private final ShoppingCartItemProductSKUSummaryRepository shoppingCartItemProductSKUSummaryRepository;
    private final ProductSKURepository productSKURepository;
    private final ProductStockChecker productStockChecker;
    private final MessageBrokerProducer shoppingCartConfirmationProducer;
    private final StockMovementManagementService stockMovementService;
    private final SalesOrderClient salesOrderClient;
    private final ProductStockManagementService productStockManagementService;

    public ShoppingCartService(ShoppingCartItemRepository shoppingCartItemRepository, ShoppingCartItemMapper shoppingCartItemProductSKUMapper, ShoppingCartItemProductSKUSummaryRepository shoppingCartItemProductSKUSummaryRepository, ProductSKURepository productSKURepository, ProductStockChecker productStockChecker, MessageBrokerProducer shoppingCartConfirmationProducer, StockMovementManagementService stockMovementService, SalesOrderClient salesOrderClient, ProductStockManagementService productStockManagementService) {
        this.shoppingCartItemRepository = shoppingCartItemRepository;
        this.shoppingCartItemProductSKUMapper = shoppingCartItemProductSKUMapper;
        this.shoppingCartItemProductSKUSummaryRepository = shoppingCartItemProductSKUSummaryRepository;
        this.productSKURepository = productSKURepository;
        this.productStockChecker = productStockChecker;
        this.shoppingCartConfirmationProducer = shoppingCartConfirmationProducer;
        this.stockMovementService = stockMovementService;
        this.salesOrderClient = salesOrderClient;
        this.productStockManagementService = productStockManagementService;
    }

    public void createItemByUserId(CreateShoppingCartItemRequest request, UUID authenticatedUserId) {
        ProductSKU sku = productSKURepository.findActiveById(request.productSKUId())
                .orElseThrow(() -> new ProductSKUNotFoundException("Product SKU not found with ID: "+request.productSKUId()));

        if (!productStockChecker.isTheProductWithStockEnough(sku.getId(), request.units()))
            throw new ProductOutOfStockException("This product haven't stock enough");

        ShoppingCartItem item = new ShoppingCartItem();
        item.setUserId(authenticatedUserId);
        item.setProductSKU(sku);
        item.setUnits(request.units());

        if (shoppingCartItemRepository.existsByProductSKUIdAndUserId(request.productSKUId(), authenticatedUserId))
            throw new ShoppingCartItemAlreadyExistsException("This product is already on shopping cart");

        shoppingCartItemRepository.save(item);
    }

    public PagedResponse<ShoppingCartItemResponse> getAllItems(UUID authenticatedUserId, Pageable pageable) {
        Page<ShoppingCartItemProductSKUSummary> page = shoppingCartItemProductSKUSummaryRepository.findAllByUserId(authenticatedUserId, pageable);

        return PagedResponse.<ShoppingCartItemResponse>builder()
                .page(page.getNumber())
                .size(page.getSize())
                .isLast(page.isLast())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .content(page.getContent().stream()
                        .map(entity -> shoppingCartItemProductSKUMapper.toResponse(entity))
                        .toList()
                )
                .build();
    }

    public void deleteItemById(UUID id, UUID authenticatedUserId) {
        ShoppingCartItem item = shoppingCartItemRepository.findActiveByIdAndUserId(id, authenticatedUserId)
                .orElseThrow(() -> new ShoppingCartItemNotFoundException("Shopping cart item not found with ID: "+id));

        item.setIsActive(false);
        shoppingCartItemRepository.save(item);
    }

    public void deleteAllItemsByUserId(UUID userId) {
        shoppingCartItemRepository.markAllAsInactiveByUserId(userId);
    }

    @Transactional
    public void confirmShoppingCart(ConfirmShoppingCartRequest request, UUID userId, String JWTBearer) {
        verifyDeliveryAddress(request.deliveryAddressId(), JWTBearer);

        List<CreateOrderMessage.OrderItem> items = shoppingCartItemProductSKUSummaryRepository.findAllByUserId(userId).stream()
                .map(shoppingCartItemProductSKUMapper::toCreateOrderMessageItem)
                .toList();

        if (items.isEmpty())
         throw new EmptyShoppingCartException("The shopping cart is empty");

        var shoppingCartConfirmation = new CreateOrderMessage(userId, items, request.deliveryAddressId());

        updateStock(items, userId);

        shoppingCartConfirmationProducer.produce(shoppingCartConfirmation);

        clearShoppingCart(userId);
    }
    private void verifyDeliveryAddress(UUID deliveryAddressId, String JWTBearer) {
        ResponseEntity<?> deliveryAddressResponse = salesOrderClient.getDeliveryAddress(deliveryAddressId, JWTBearer);

        if (!deliveryAddressResponse.getStatusCode().is2xxSuccessful())
            throw new DeliveryAddressNotFoundException("Delivery address not found with ID: "+deliveryAddressId);
    }

    private void updateStock(List<CreateOrderMessage.OrderItem> items, UUID userId) {
        for (var item : items) {
            productStockManagementService.reduceProductStock(item.productSKUId(), item.units());
            stockMovementService.registerSale(item.productSKUId(), item.units(), userId);
        }
    }

    private void clearShoppingCart(UUID userId) {
        this.deleteAllItemsByUserId(userId);
    }
}
