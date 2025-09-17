package com.sparta.orderservice.order.infrastructure.mq;

import java.io.Serializable;
import java.util.UUID;

/** 상품 서비스가 소비할 커맨드(직렬화 가능) */
public record DecreaseStockCommand(
        UUID productId,
        Integer quantity,
        String orderId,        // 멱등 키(주문ID)
        long requestedAtEpoch  // 디버깅/관찰용
) implements Serializable {}
