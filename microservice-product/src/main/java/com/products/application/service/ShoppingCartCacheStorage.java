package com.products.application.service;

import com.products.application.dto.catalogue.ShoppingCart;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.UUID;

@Service
public class ShoppingCartCacheStorage {
    private static final String CACHE_NAME = "shopping-cart";

    @Cacheable(value = CACHE_NAME, key = "#userId")
    public ShoppingCart get(UUID userId){
        return new ShoppingCart(new ArrayList<>());
    }

    @CachePut(value = CACHE_NAME, key = "#userId")
    public ShoppingCart update(UUID userId, ShoppingCart shoppingCart){
        return  shoppingCart;
    }

    @CacheEvict(value =  CACHE_NAME, key = "#userId")
    public void clear(UUID userId){}
}
