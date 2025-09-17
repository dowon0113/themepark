package com.business.productservice.domain.product.repository;

import com.business.productservice.domain.product.entity.StockDecreaseLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface StockDecreaseLogRepository extends JpaRepository<StockDecreaseLog, String> {
    @Modifying
    @Query(value = """
        INSERT INTO product_service.stock_decrease_log(order_id, product_id, quantity, processed_at)
        VALUES (:orderId, :productId, :q, now())
        ON CONFLICT (order_id) DO NOTHING
        """, nativeQuery = true)
    int claimOnce(@Param("orderId") String orderId,
                  @Param("productId") UUID productId,
                  @Param("q") Integer q);
}
