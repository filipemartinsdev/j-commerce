package com.products.application.service;

import com.products.application.dto.PagedResponse;
import com.products.application.dto.catalogue.CreateShoppingCartItemRequest;
import com.products.application.dto.catalogue.ShoppingCartItemResponse;
import com.products.application.exception.*;
import com.products.application.service.mapper.ShoppingCartItemMapper;
import com.products.domain.entity.ProductSKU;
import com.products.domain.entity.ProductStock;
import com.products.domain.entity.ShoppingCartItem;
import com.products.domain.entity.ShoppingCartItemProductSKUResume;
import com.products.infra.persistence.ProductSKURepository;
import com.products.infra.persistence.ProductStockRepository;
import com.products.infra.persistence.ShoppingCartItemProductSKUResponseRepository;
import com.products.infra.persistence.ShoppingCartItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ShoppingCartService {
    private final ShoppingCartItemRepository shoppingCartItemRepository;
    private final ShoppingCartItemMapper shoppingCartItemProductSKUMapper;
    private final ShoppingCartItemProductSKUResponseRepository shoppingCartItemProductSKUResponseRepository;
    private final ProductSKURepository productSKURepository;
    private final ProductStockChecker productStockChecker;

    public ShoppingCartService(ShoppingCartItemRepository shoppingCartItemRepository, ShoppingCartItemMapper shoppingCartItemProductSKUMapper, ShoppingCartItemProductSKUResponseRepository shoppingCartItemProductSKUResponseRepository, ProductSKURepository productSKURepository, ProductStockChecker productStockChecker) {
        this.shoppingCartItemRepository = shoppingCartItemRepository;
        this.shoppingCartItemProductSKUMapper = shoppingCartItemProductSKUMapper;
        this.shoppingCartItemProductSKUResponseRepository = shoppingCartItemProductSKUResponseRepository;
        this.productSKURepository = productSKURepository;
        this.productStockChecker = productStockChecker;
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
        Page<ShoppingCartItemProductSKUResume> page = shoppingCartItemProductSKUResponseRepository.findAllByUserId(authenticatedUserId, pageable);

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
}
