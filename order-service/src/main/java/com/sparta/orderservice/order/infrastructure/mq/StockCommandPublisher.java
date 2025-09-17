package com.sparta.orderservice.order.infrastructure.mq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

@Component
public class StockCommandPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public StockCommandPublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${app.mq.exchange}") String exchange,
            @Value("${app.mq.routingKey}") String routingKey
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    /** 주문 트랜잭션이 커밋된 뒤에만 발행 (half-commit 방지) */
    public void publishAfterCommit(DecreaseStockCommand cmd) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            rabbitTemplate.convertAndSend(exchange, routingKey, ensureOrderId(cmd));
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override public void afterCommit() {
                rabbitTemplate.convertAndSend(exchange, routingKey, ensureOrderId(cmd));
            }
        });
    }

    private DecreaseStockCommand ensureOrderId(DecreaseStockCommand cmd) {
        if (cmd.orderId() != null && !cmd.orderId().isBlank()) return cmd;
        return new DecreaseStockCommand(
                cmd.productId(),
                cmd.quantity(),
                UUID.randomUUID().toString(), // 주문ID가 비어있을 경우 멱등키 생성
                cmd.requestedAtEpoch()
        );
    }
}
