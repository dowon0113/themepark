package com.business.productservice.infrastructure.persistence.product;

import com.business.productservice.domain.product.entity.StockEntity;
import com.business.productservice.domain.product.repository.StockRepository;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StockJpaRepository extends JpaRepository<StockEntity, UUID>, StockRepository{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM StockEntity s WHERE s.id = :id")
    Optional<StockEntity> findByIdWithPessimisticLock(@Param("id") UUID id);

    Optional<StockEntity> findById(UUID id);

    // 재고가 충분할 때만 차감 (EVENT 한정) — 성공 시 1, 실패 시 0
    @Modifying
    @Transactional
    @Query(value = """
    UPDATE product_service.p_product_stocks s
    SET stock = stock - :q
    FROM product_service.p_products p
    WHERE s.product_stock_id = :pid
      AND s.product_stock_id = p.product_id
      AND p.product_type = 'EVENT'
      AND s.stock >= :q
    """, nativeQuery = true)
    int decreaseAtomically(@Param("pid") UUID productId, @Param("q") Integer quantity);


    @Query(value = "SELECT stock FROM p_product_stocks WHERE product_stock_id = :pid", nativeQuery = true)
    Integer getCurrentStock(@Param("pid") UUID productId);

    // 예약(동기 빠른 경로): 재고가 충분할 때만 reserved += q
    @Modifying
    @Transactional
    @Query(value = """
        UPDATE product_service.p_product_stocks s
        SET reserved = reserved + :q
        FROM product_service.p_products p
        WHERE s.product_stock_id = :pid
          AND s.product_stock_id = p.product_id
          AND (s.stock - s.reserved) >= :q
        """, nativeQuery = true)
    int reserveAtomically(@Param("pid") UUID productId, @Param("q") Integer quantity);

    // (B) 확정: reserved -= q, stock -= q, (선택) sold += q
    @Modifying
    @Transactional
    @Query(value = """
        UPDATE product_service.p_product_stocks s
        SET reserved = reserved - :q,
            stock    = stock    - :q,
            sold     = sold     + :q   -- ★ 집계 원하면 추가
        FROM product_service.p_products p
        WHERE s.product_stock_id = :pid
          AND s.product_stock_id = p.product_id
          AND s.reserved >= :q
          AND s.stock    >= :q
        """, nativeQuery = true)
    int confirmAtomically(@Param("pid") UUID productId, @Param("q") Integer quantity);

    // (C) 보상: 예약 해제(확정 실패 시 reserved 되돌리기)
    @Modifying
    @Transactional
    @Query(value = """
        UPDATE product_service.p_product_stocks s
        SET reserved = reserved - :q
        FROM product_service.p_products p
        WHERE s.product_stock_id = :pid
          AND s.product_stock_id = p.product_id
          AND s.reserved >= :q
        """, nativeQuery = true)
    int releaseReservation(@Param("pid") UUID productId, @Param("q") Integer quantity);


}
