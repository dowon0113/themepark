package com.business.productservice.infrastructure.mq;

import com.business.productservice.domain.product.entity.StockDecreaseLog;
import com.business.productservice.domain.product.repository.StockDecreaseLogRepository;
import com.business.productservice.domain.product.repository.StockRepository;
import com.github.themepark.common.application.exception.CustomException;
import com.business.productservice.application.exception.ProductExceptionCode;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockDecreaseConsumer {

    private final StockRepository stockRepository;
    private final StockDecreaseLogRepository logRepository;
    private final StockResultPublisher eventPublisher;

    @RabbitListener(queues = "${app.mq.queue}", containerFactory = "manualAckFactory")
    @Transactional
    public void onMessage(DecreaseStockCommand cmd, Channel ch, Message message) throws IOException {

        final long tag = message.getMessageProperties().getDeliveryTag();
        final UUID productId = cmd.productId();
        final Integer q = Math.max(1, cmd.quantity());
        final String orderId = (cmd.orderId() == null || cmd.orderId().isBlank())
                ? "MSG-" + message.getMessageProperties().getMessageId()
                : cmd.orderId();

        // === 커밋 후 ACK 유틸 ===
        Runnable ackAfterCommit = () ->
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override public void afterCommit() {
                        try { ch.basicAck(tag, false); } catch (IOException e) { log.error("ACK fail", e); }
                    }
                });

        try {
            // 0) 멱등 확인: 이미 성공 처리한 주문이면 바로 ACK
            if (logRepository.existsById(orderId)) {
                log.info("Idempotent hit - orderId={} already processed", orderId);
                ackAfterCommit.run();
                return;
            }
//            int claimed = logRepository.claimOnce(orderId, productId, q);
//            if (claimed == 0) {
//                log.info("Idempotent skip (already claimed) - orderId={}", orderId);
//                ackAfterCommit.run();
//                return;
//            }

            // 1) 확정 시도: reserved -= q, stock -= q (원자적)
            int updated = stockRepository.confirmAtomically(productId, q);

            if (updated == 1) {
                // 2) 성공 → 성공 멱등 로그 저장 (성공의 증표)
                logRepository.save(new StockDecreaseLog(orderId, productId, q));
                // 3) 결과 이벤트 (성공)
                eventPublisher.publishDecreased(orderId, productId, q);
                ackAfterCommit.run();
                return;
//                // 2) 성공 이벤트
//                eventPublisher.publishDecreased(orderId, productId, q);
//                ackAfterCommit.run();
//                return;
            }

            // 4) 실패(updated=0) → 보상: 예약 해제 시도 (있으면 되돌리고, 없으면 0건)
            int released = stockRepository.releaseReservation(productId, q);
            log.warn("Confirm failed → released={}, pid={}, q={}", released, productId, q);

            // 5) 결과 이벤트 (거절)
            eventPublisher.publishRejected(orderId, productId, q, "INSUFFICIENT_RESERVED_OR_STOCK");
            ackAfterCommit.run();

        } catch (DataIntegrityViolationException dup) {
            // 동시 처리 중 로그 PK 충돌 → 이미 성공 처리된 것으로 간주
            log.info("Idempotent log race - orderId={}", orderId);
            ackAfterCommit.run();

        } catch (TransientDataAccessException transientEx) {
            // 일시적 DB 문제 → 재시도
            log.warn("Transient DB error, requeue. pid={}, err={}", productId, transientEx.getMessage());
            ch.basicNack(tag, false, true);

        } catch (Exception fatal) {
            // 알 수 없는 오류 → DLQ (운영 정책에 따라 requeue=true도 가능)
            log.error("Fatal error → DLQ. pid={}, orderId={}", productId, orderId, fatal);
            ch.basicReject(tag, false);
        }
    }
}