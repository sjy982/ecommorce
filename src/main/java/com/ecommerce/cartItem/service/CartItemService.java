package com.ecommerce.cartItem.service;

import java.util.List;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ecommerce.cartItem.model.CartItem;
import com.ecommerce.cartItem.projection.CartItemProjection;
import com.ecommerce.cartItem.repository.CartItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartItemService {
    private final CartItemRepository cartItemRepository;

    public List<CartItemProjection> findCartItemsByCartId(long cartId) {
        return cartItemRepository.findCartItemsByCartId(cartId);
    }

    public void save(CartItem cartItem) {
        cartItemRepository.save(cartItem);
    }

    public List<CartItem> findByIn(List<Long> cartItemIds) {
        List<CartItem> cartItems = cartItemRepository.findByIdIn(cartItemIds);

        if(cartItemIds.size() != cartItems.size()) {
            throw new UsernameNotFoundException("cartItem not found");
        }

        return cartItems;
    }

    public void deleteByIdIn(List<Long> cartItemIds) {
        cartItemRepository.deleteByIdIn(cartItemIds);
    }
}
