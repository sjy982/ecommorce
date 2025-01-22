package com.ecommerce.cartItem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecommerce.cartItem.model.CartItem;
import com.ecommerce.cartItem.projection.CartItemProjection;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartId(long cartId);

    @Query("SELECT p.id AS productId, p.name AS productName, p.price AS productPrice, ci.id AS cartItemId, ci.quantity AS quantity " +
           "FROM CartItem ci " +
           "JOIN ci.product p " +
           "WHERE ci.cart.id = :cartId")
    List<CartItemProjection> findCartItemsByCartId(@Param("cartId") long cartId);
}
