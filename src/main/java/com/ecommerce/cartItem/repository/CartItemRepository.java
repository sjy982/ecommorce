package com.ecommerce.cartItem.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.cartItem.model.CartItem;
import com.ecommerce.cartItem.projection.CartItemProjection;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartId(long cartId);

    @Query("SELECT p.id AS productId, p.name AS productName, p.price AS productPrice, ci.id AS cartItemId, ci.quantity AS quantity " +
           "FROM CartItem ci " +
           "JOIN ci.product p " +
           "WHERE ci.cart.id = :cartId")
    List<CartItemProjection> findCartItemsByCartId(@Param("cartId") long cartId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.id IN :cartItemIds")
    List<CartItem> findByIdIn(@Param("cartItemIds") List<Long> cartItemIds);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.id IN :cartItemIds")
    void deleteByIdIn(@Param("cartItemIds") List<Long> cartItemIds);

}
