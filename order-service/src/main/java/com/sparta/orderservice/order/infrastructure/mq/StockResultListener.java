package com.sparta.orderservice.order.infrastructure.mq;

import com.sparta.orderservice.order.infrastructure.repository.OrderServiceJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockResultListener {

    private final OrderServiceJpaRepository orderRepository;

    @RabbitListener(queues = "${app.mq.result-queue}")
    @Transactional
    public void onResult(StockResult evt) {
        var orderId = UUID.fromString(evt.orderId());
        var order = orderRepository.findById(orderId).orElseThrow();

        switch (evt.type()) {
            case DECREASED -> order.confirm();  // CONFIRMED
            case REJECTED  -> order.cancel();   // CANCELLED
        }
    }
}

