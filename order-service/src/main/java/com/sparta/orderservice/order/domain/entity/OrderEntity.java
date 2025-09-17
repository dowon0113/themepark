package com.sparta.orderservice.order.domain.entity;

import com.sparta.orderservice.order.domain.OrderStatus;
import com.sparta.orderservice.order.presentation.dto.v1.request.ReqOrderPutDtoApiV1;
import com.sparta.orderservice.order.presentation.dto.v1.request.ReqOrdersPostDtoApiV1;
import com.sparta.orderservice.payment.domain.vo.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import com.github.themepark.common.domain.entity.BaseEntity;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "p_orders")
public class OrderEntity extends BaseEntity {
    @Id
    @UuidGenerator
    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "slack_id")
    private String slackId;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "amount")
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;

    @Column(name = "payment_id")
    private UUID paymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    public static OrderEntity createOrder(UUID productId, Integer amount, String slackId, Long userId){
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.userId = userId;
        orderEntity.productId = productId;
        orderEntity.amount = amount;
        orderEntity.slackId = slackId;
        orderEntity.paymentStatus = PaymentStatus.WAITING;
        orderEntity.paymentId = null;

        return orderEntity;
    }

    public static void updateOrder(OrderEntity orderEntity, UUID paymentId, PaymentStatus paymentStatus) {
        orderEntity.paymentId = paymentId;
        orderEntity.paymentStatus = paymentStatus;
    }

    public static OrderEntity createOrderService(UUID productId, Integer amount){
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.productId = productId;
        orderEntity.amount = amount;
        orderEntity.status = OrderStatus.PENDING; //최초는 팬딩 상태
        return orderEntity;
    }

    //== 상태 전환 메서드 ==//
    public void confirm() {
        this.status = OrderStatus.CONFIRMED;
    }

    public void cancel() {
        this.status = OrderStatus.CANCELLED;
    }

}
