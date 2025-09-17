package com.sparta.orderservice.order.domain;

public enum OrderStatus { 
    PENDING, //예약만 성공, 확정 대기
    CONFIRMED, //상품 서비스에서 확정됨
    CANCELLED //실패/재고 부족으로 취소됨
}

