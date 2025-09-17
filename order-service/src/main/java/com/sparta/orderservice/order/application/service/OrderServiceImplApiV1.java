package com.sparta.orderservice.order.application.service;

import com.sparta.orderservice.order.application.dto.reponse.ReqOrderPostDTOApiV1;
import com.sparta.orderservice.order.application.dto.reponse.ResOrderPostDTOApiV1;
import com.sparta.orderservice.order.domain.entity.OrderEntity;
import com.sparta.orderservice.order.infrastructure.feign.ProductFeignClientApi;
import com.sparta.orderservice.order.infrastructure.mq.DecreaseStockCommand;
import com.sparta.orderservice.order.infrastructure.mq.StockCommandPublisher;
import com.sparta.orderservice.order.infrastructure.repository.OrderServiceJpaRepository;
import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderServiceImplApiV1 implements OrderServiceApiV1 {

    private final OrderServiceJpaRepository orderRepository;
    private final StockCommandPublisher stockCommandPublisher;
    private final ProductFeignClientApi productClient;

    @Override
    public ResOrderPostDTOApiV1 postBy(ReqOrderPostDTOApiV1 reqDto){

        // 0) 파라미터 정리/방어
        final UUID productId = reqDto.getProductId();
        final Integer qty = Math.max(1, reqDto.getAmount());

        // 1) 상품 서비스에 "소프트 예약" 동기 호출
        boolean reserved = false;
        try {
            ResponseEntity<Void> resp = productClient.reserve(productId, qty);
            reserved = (resp != null && resp.getStatusCode().is2xxSuccessful());
        } catch (FeignException.Conflict e) {
            // 409 → 재고부족/품절
            reserved = false;
        } catch (FeignException.NotFound e) {
            // 404 → 상품 없음 등 상황에 맞게 예외 전환
            throw new RuntimeException("상품을 찾을 수 없습니다: " + productId, e);
        } catch (FeignException e) {
            // 그 외 (5xx/네트워크) → 호출자에게 에러 전파 (서킷브레이커/재시도는 별도 구성)
            throw new RuntimeException("상품 서비스 예약 호출 실패", e);
        }

        if (!reserved) {
            // 2) 예약 실패 → 즉시 품절 응답 (예외 타입은 프로젝트 표준에 맞게 교체)
            throw new RuntimeException("품절되었습니다."); // 예: throw new SoldOutException();
        }

        // 3) 예약 성공 → 주문을 PENDING(혹은 RESERVED) 상태로 저장
        OrderEntity order = OrderEntity.createOrderService(productId, qty);
        OrderEntity saved = orderRepository.save(order);

        // 4) 커밋 이후 재고 차감 확정 커맨드 비동기 발행 (half-commit 방지)
        stockCommandPublisher.publishAfterCommit(
                new DecreaseStockCommand(
                        saved.getProductId(),
                        qty,
                        saved.getOrderId().toString(), // 멱등키
                        System.currentTimeMillis()
                )
        );
        // 5) 즉시 응답 (이 시점의 주문은 PENDING 상태)
        return ResOrderPostDTOApiV1.of(saved);
    }

    @Override
    public ResOrderPostDTOApiV1 postByWithLock (ReqOrderPostDTOApiV1 reqDto){

        //1. 주문 등록
        final UUID productId = reqDto.getProductId();
        final Integer qty = Math.max(1, reqDto.getAmount());

        OrderEntity order = OrderEntity.createOrderService(productId, qty);
        OrderEntity saved = orderRepository.save(order);

        //2. 상품 서비스 쪽에 동기로 재고 차감 요청
        try {
            productClient.postDecreaseById(productId);
        } catch (FeignException.Conflict e) {

        }

        return ResOrderPostDTOApiV1.of(saved);
    }
}
