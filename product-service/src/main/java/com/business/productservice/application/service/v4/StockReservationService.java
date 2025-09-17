package com.business.productservice.application.service.v4;

import com.business.productservice.infrastructure.persistence.product.StockJpaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockReservationService {

    private final StockJpaRepository stockRepository;

    /** 재고가 충분하면 reserved += qty (원자적), 성공 시 true */
    @Transactional
    public boolean reserve(UUID productId, Integer qty) {
        Integer q = Math.max(1, qty);
        int updated = stockRepository.reserveAtomically(productId, q);
        return updated == 1;
    }
}
