package com.business.productservice.domain.product.repository;

import com.business.productservice.domain.product.entity.StockEntity;
import org.hibernate.LockMode;

import java.util.Optional;
import java.util.UUID;

public interface StockRepository {
    Optional<StockEntity> findByIdWithPessimisticLock(UUID id);
    Optional<StockEntity> findById(UUID id);

    /** 재고가 충분할 때만 차감 (EVENT 한정) — 성공 시 1, 실패 시 0 */
    int decreaseAtomically(UUID productId, Integer quantity);

    /** 현재 재고 조회 (슬랙 알림 판단 등) */
    Integer getCurrentStock(UUID productId);

    /** 예약 API */
    int reserveAtomically(UUID productId, Integer quantity);

    int confirmAtomically(UUID productId, Integer quantity);

    int releaseReservation(UUID productId, Integer quantity);

}
