package com.sparta.orderservice.order.application.dto.reponse;

import com.sparta.orderservice.order.domain.entity.OrderEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResOrderPostDTOApiV1 {

    private Order order;

    public static ResOrderPostDTOApiV1 of(OrderEntity orderEntity){
        return ResOrderPostDTOApiV1.builder()
                .order(ResOrderPostDTOApiV1.Order.from(orderEntity))
                .build();
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Order{
        private UUID id;
        private Integer amount;

        public static Order from(OrderEntity orderEntity){
            return Order.builder()
                    .id(orderEntity.getProductId())
                    .amount(orderEntity.getAmount())
                    .build();
        }
    }

}
