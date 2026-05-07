package com.products.application.service;

import com.products.application.dto.PagedResponse;
import com.products.application.dto.catalogue.CreateWishlistItemRequest;
import com.products.application.dto.catalogue.WishlistItemResponse;
import com.products.application.exception.ProductSKUNotFoundException;
import com.products.application.exception.WishlistItemAlreadyExistsException;
import com.products.application.exception.WishlistItemNotFoundException;
import com.products.application.factory.PagedResponseFactory;
import com.products.application.service.mapper.WishlistItemMapper;
import com.products.domain.entity.ProductSKU;
import com.products.domain.entity.WishlistItem;
import com.products.domain.entity.WishlistItemProductSKUResume;
import com.products.infra.persistence.ProductSKURepository;
import com.products.infra.persistence.WishlistItemProductSKUResumeRepository;
import com.products.infra.persistence.WishlistItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class WishlistService {
    private final WishlistItemRepository wishlistItemRepository;
    private final ProductSKURepository productSKURepository;
    private final WishlistItemProductSKUResumeRepository wishlistItemProductSKUResumeRepository;
    private final WishlistItemMapper wishlistItemMapper;
    private final ProductDiscountCalculator productDiscountCalculator;
    private final PagedResponseFactory<WishlistItemResponse> pagedResponseFactory;

    public WishlistService(WishlistItemRepository wishlistItemRepository, ProductSKURepository productSKURepository, WishlistItemProductSKUResumeRepository wishlistItemProductSKUResumeRepository, WishlistItemMapper wishlistItemMapper, ProductDiscountCalculator productDiscountCalculator, PagedResponseFactory<WishlistItemResponse> pagedResponseFactory) {
        this.wishlistItemRepository = wishlistItemRepository;
        this.productSKURepository = productSKURepository;
        this.wishlistItemProductSKUResumeRepository = wishlistItemProductSKUResumeRepository;
        this.wishlistItemMapper = wishlistItemMapper;
        this.productDiscountCalculator = productDiscountCalculator;
        this.pagedResponseFactory = pagedResponseFactory;
    }

    public PagedResponse<WishlistItemResponse> getAllItems(UUID authenticatedUser, Pageable pageable){
        Page<WishlistItemProductSKUResume> page = wishlistItemProductSKUResumeRepository.findAllByUserId(authenticatedUser, pageable);

        return pagedResponseFactory.fromPage(page, (entity) -> {
                int discount = productDiscountCalculator.getDiscountPercent(entity.getOriginalPrice(), entity.getCurrentPrice());
                return wishlistItemMapper.toResponse(entity);
        });
    }
    public void createItem(CreateWishlistItemRequest request, UUID authenticatedUserId) {
        ProductSKU productSKU = productSKURepository.findById(request.productSKUId())
                .orElseThrow(() -> new ProductSKUNotFoundException("ProductSKU not found with ID: "+request.productSKUId()));

        WishlistItem item = new WishlistItem();
        item.setUserId(authenticatedUserId);
        item.setProductSKU(productSKU);

        if (wishlistItemRepository.existsByProductSKUIdAndUserId(productSKU.getId(), authenticatedUserId))
            throw new WishlistItemAlreadyExistsException("This product is already on wishlist");

        wishlistItemRepository.save(item);
    }

    public void deleteItem(UUID id, UUID authenticatedUserId) {
        WishlistItem item = wishlistItemRepository.findActiveByIdAndUserId(id, authenticatedUserId)
                .orElseThrow(() -> new WishlistItemNotFoundException("WishlistItem not found with ID: "+id));

        item.setIsActive(false);
        wishlistItemRepository.save(item);
    }

    public void deleteAllItemsByUserId(UUID userId) {
        wishlistItemRepository.markAllAsInactiveByUserId(userId);
    }
}
