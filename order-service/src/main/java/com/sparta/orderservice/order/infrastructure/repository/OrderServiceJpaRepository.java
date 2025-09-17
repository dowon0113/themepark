package com.sparta.orderservice.order.infrastructure.repository;

import com.sparta.orderservice.order.domain.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderServiceJpaRepository extends JpaRepository<OrderEntity, UUID> {
}
