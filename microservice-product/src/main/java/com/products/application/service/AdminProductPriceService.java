package com.products.application.service;

import com.products.application.dto.PagedResponse;
import com.products.application.dto.admin.ProductSKUPriceResponse;
import com.products.application.dto.admin.UpdateProductSKUPriceRequest;
import com.products.application.exception.InvalidProductPriceTypeException;
import com.products.application.exception.ProductSKUNotFoundException;
import com.products.application.exception.ProductSKUPriceNotFoundException;
import com.products.application.exception.ProductSKUWithoutBasePriceException;
import com.products.application.service.mapper.ProductSKUPriceMapper;
import com.products.domain.entity.PriceType;
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
import java.util.List;
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
        ProductSKU sku = retrieveProductSKUById(request.productSKUId());

        if(request.priceTypeId() > 1 && !haveBasePrice(request.productSKUId()))
            throw new ProductSKUWithoutBasePriceException("This product haven't a base price yet. Before set a special price, set a base price without expiration time.");

        ProductSKUPrice newPrice = new ProductSKUPrice();
        newPrice.setProductSKU(sku);
        newPrice.setPrice(request.price());
        newPrice.setPriceType(retrievePriceTypeById(request.priceTypeId()));

        if (request.startAt().isPresent())
            newPrice.setStartAt(request.startAt().get());
        else
            newPrice.setStartAt(Instant.now());
        if (request.endAt().isPresent())
            newPrice.setEndAt(request.endAt().get());

        return productSKUPriceMapper.toResponse(productSKUPriceRepository.save(newPrice));
    }

    private boolean haveBasePrice(UUID productSKUId) {
        List<ProductSKUPrice> basePrices = productSKUPriceRepository.findAllActiveBasePriceByProductSKUId(productSKUId);
        return !basePrices.isEmpty();
    }

    private ProductSKU retrieveProductSKUById(UUID id){
        return productSKURepository.findById(id)
                .orElseThrow(() -> new ProductSKUNotFoundException("ProductSKU not found with ID: "+id));
    }

    private PriceType retrievePriceTypeById(Integer id){
        return priceTypeRepository.findById(id)
                .orElseThrow(() -> new InvalidProductPriceTypeException("Invalid PriceType with ID: "+id));
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
