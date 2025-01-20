package com.ecommerce.cartItem.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.cart.model.Cart;
import com.ecommerce.cartItem.model.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartId(long cartId);
}
