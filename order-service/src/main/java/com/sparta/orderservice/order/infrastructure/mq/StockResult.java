package com.sparta.orderservice.order.infrastructure.mq;

import java.util.UUID;

public record StockResult(
        String orderId,
        UUID productId,
        Integer quantity,
        Type type,
        String reason
) implements java.io.Serializable {
    public enum Type { DECREASED, REJECTED }
}
