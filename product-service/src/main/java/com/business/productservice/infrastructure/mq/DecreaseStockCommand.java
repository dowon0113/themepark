package com.business.productservice.infrastructure.mq;

import java.io.Serializable;
import java.util.UUID;

public record DecreaseStockCommand(
        UUID productId,
        Integer quantity,
        String orderId,
        long requestedAtEpoch
) implements Serializable {}
