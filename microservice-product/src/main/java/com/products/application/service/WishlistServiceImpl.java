package com.products.application.service;

import com.products.application.dto.catalogue.WishlistItemResponse;
import com.products.application.exception.ProductNotFoundException;
import com.products.application.exception.WishlistItemAlreadyExistsException;
import com.products.application.exception.WishlistItemNotFoundException;
import com.products.application.service.mapper.WishlistItemMapper;
import com.products.domain.entity.WishlistItem;
import com.products.infra.persistence.ProductRepository;
import com.products.infra.persistence.WishlistItemRepository;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class WishlistServiceImpl implements WishlistService {
    private final WishlistItemRepository wishlistRepository;
    private final WishlistItemMapper wishlistItemMapper;
    private final ProductRepository productRepository;

    public WishlistServiceImpl(WishlistItemRepository wishlistRepository, WishlistItemMapper wishlistItemMapper, ProductRepository productRepository) {
        this.wishlistRepository = wishlistRepository;
        this.wishlistItemMapper = wishlistItemMapper;
        this.productRepository = productRepository;
    }

    @Override
    public Window<WishlistItemResponse> get(UUID userId, ScrollPosition position, Limit limit) {
        return wishlistRepository.findAllByUserId(userId, position, limit)
                .map(wishlistItemMapper::toResponse);
    }

    @Override
    public void add(UUID userId, String productId) {
        if (wishlistRepository.existsByUserIdAndProductId(userId, productId))
            throw new WishlistItemAlreadyExistsException("Wishlist item already exists");

        var product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found by ID: "+productId));

        var item = new WishlistItem();
        item.setUserId(userId);
        item.setProductId(productId);
        item.setName(product.getName());
        wishlistRepository.save(item);
    }

    @Override
    public void remove(UUID userId, String productId) {
        var wishlistItem = wishlistRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new WishlistItemNotFoundException("Wishlist item not found"));

        wishlistRepository.delete(wishlistItem);
    }

    @Override
    public void clear(UUID userId) {
        wishlistRepository.deleteAllByUserId(userId);
    }
}
