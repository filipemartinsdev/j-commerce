package com.products.application.service;

import com.products.application.dto.PagedResponse;
import com.products.application.dto.admin.ProductSKUPriceResponse;
import com.products.application.dto.admin.UpdateProductSKUPriceRequest;
import com.products.application.exception.ProductSKUNotFoundException;
import com.products.application.exception.ProductSKUPriceNotFoundException;
import com.products.application.service.mapper.ProductSKUPriceMapper;
import com.products.domain.entity.ProductSKU;
import com.products.domain.entity.ProductSKUPrice;
import com.products.infra.persistence.PriceTypeRepository;
import com.products.infra.persistence.ProductSKUPriceRepository;
import com.products.infra.persistence.ProductSKURepository;
import com.products.application.dto.admin.CreateProductSKUPrice;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class AdminProductPriceService {
    private final ProductSKUPriceRepository productSKUPriceRepository;
    private final ProductSKUPriceMapper productSKUPriceMapper;
    private final ProductSKURepository productSKURepository;
    private final PriceTypeRepository priceTypeRepository;

    public AdminProductPriceService(ProductSKUPriceRepository productSKUPriceRepository, ProductSKUPriceMapper productSKUPriceMapper, ProductSKURepository productSKURepository, PriceTypeRepository priceTypeRepository) {
        this.productSKUPriceRepository = productSKUPriceRepository;
        this.productSKUPriceMapper = productSKUPriceMapper;
        this.productSKURepository = productSKURepository;
        this.priceTypeRepository = priceTypeRepository;
    }

    public PagedResponse<ProductSKUPriceResponse> getAllPrices(Pageable pageable) {
        Page<ProductSKUPrice> page = productSKUPriceRepository.findAllActive(pageable);

        return PagedResponse.<ProductSKUPriceResponse>builder()
                .page(page.getNumber())
                .size(page.getSize())
                .isLast(page.isLast())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .content(page.getContent().stream()
                        .map(entity -> productSKUPriceMapper.toResponse(entity))
                        .toList()
                )
                .build();
    }

    public PagedResponse<ProductSKUPriceResponse> getAllPricesByProductSKUId(UUID productSKUId, Pageable pageable) {
        Page<ProductSKUPrice> page = productSKUPriceRepository.findAllActiveByProductSKUId(productSKUId, pageable);

        return PagedResponse.<ProductSKUPriceResponse>builder()
                .page(page.getNumber())
                .size(page.getSize())
                .isLast(page.isLast())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .content(page.getContent().stream()
                        .map(entity -> productSKUPriceMapper.toResponse(entity))
                        .toList()
                )
                .build();
    }

    public ProductSKUPriceResponse create(CreateProductSKUPrice request) {
        ProductSKU sku = productSKURepository.findById(request.productSKUId())
                .orElseThrow(() -> new ProductSKUNotFoundException("ProductSKU not found with ID: "+request.productSKUId()));

        ProductSKUPrice price = new  ProductSKUPrice();
        price.setProductSKU(sku);
        price.setPrice(request.price());
        price.setPriceType(priceTypeRepository.getReferenceById(
            request.priceTypeId()
        ));

        if (request.startAt().isPresent())
            price.setStartAt(request.startAt().get());
        else
            price.setStartAt(Instant.now());

        if (request.endAt().isPresent())
            price.setEndAt(request.endAt().get());

        return productSKUPriceMapper.toResponse(productSKUPriceRepository.save(price));
    }

    public void deleteById(UUID id) {
        ProductSKUPrice price = productSKUPriceRepository.findById(id)
                .orElseThrow(() -> new ProductSKUPriceNotFoundException("ProductSKUPrice not found with ID: "+id));

        if (!price.getIsActive())
            throw new ProductSKUPriceNotFoundException("ProductSKUPrice not found with ID: "+id);

        price.setIsActive(false);
        productSKUPriceRepository.save(price);
    }

    public ProductSKUPriceResponse update(UUID productSKUPriceId, UpdateProductSKUPriceRequest request) {
        ProductSKUPrice price = productSKUPriceRepository.findById(productSKUPriceId)
                .orElseThrow(() -> new ProductSKUPriceNotFoundException("ProductSKUPrice not found with ID: "+productSKUPriceId));

        if(request.price().isPresent())
            price.setPrice(request.price().get());

        if (request.priceType().isPresent())
            price.setPriceType(
                    priceTypeRepository.getReferenceById(request.priceType().get())
            );

        if (request.startAt().isPresent())
            price.setStartAt(request.startAt().get());

        if (request.endAt().isPresent())
            price.setEndAt(request.endAt().get());

        return productSKUPriceMapper.toResponse(productSKUPriceRepository.save(price));
    }

    @Transactional
    public void deleteAllByProductSKUId(UUID productSKUId) {
        productSKUPriceRepository.setInactiveAllByProductSKUId(productSKUId);
    }
}
