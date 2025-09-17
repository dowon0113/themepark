package com.business.productservice.infrastructure.mq;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class StockResultPublisher {
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.mq.result-exchange}") String resultExchange;

    public void publishDecreased(String orderId, UUID productId, Integer qty) {
        rabbitTemplate.convertAndSend(resultExchange, "stock.decreased",
                new StockResult(orderId, productId, qty, StockResult.Type.DECREASED, null));
    }

    public void publishRejected(String orderId, UUID productId, Integer qty, String reason) {
        rabbitTemplate.convertAndSend(resultExchange, "stock.rejected",
                new StockResult(orderId, productId, qty, StockResult.Type.REJECTED, reason));
    }
}

