package com.ecommerce.order.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.order.model.Orders;

public interface OrderRepository extends JpaRepository<Orders, Long> {
    Optional<Orders> findByUserProviderId(String userProviderId);
}
