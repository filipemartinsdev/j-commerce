package com.products.application.service;

import com.products.application.dto.admin.StockMovementTypeResponse;
import com.products.application.service.mapper.StockMovementTypeMapper;
import com.products.infra.persistence.StockMovementTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockMovementTypeService {
    private final StockMovementTypeRepository stockMovementTypeRepository;
    private final StockMovementTypeMapper stockMovementTypeMapper;

    public StockMovementTypeService(StockMovementTypeRepository stockMovementTypeRepository, StockMovementTypeMapper stockMovementTypeMapper) {
        this.stockMovementTypeRepository = stockMovementTypeRepository;
        this.stockMovementTypeMapper = stockMovementTypeMapper;
    }

    public List<StockMovementTypeResponse> getAll(){
        return stockMovementTypeRepository.findAll().stream()
                .map(stockMovementTypeMapper::toResponse)
                .toList();
    }
}
