package com.products.infra.persistence;

import com.products.domain.entity.PriceType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceTypeRepository extends JpaRepository<PriceType, Integer> {
}
