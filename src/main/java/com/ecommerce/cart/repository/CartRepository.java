package com.ecommerce.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.cart.model.Cart;

public interface CartRepository extends JpaRepository<Cart, Long> {
}
