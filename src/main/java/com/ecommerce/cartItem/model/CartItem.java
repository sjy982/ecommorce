package com.ecommerce.cartItem.model;

import com.ecommerce.cart.model.Cart;
import com.ecommerce.product.model.Product;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "cart_item")
@Getter
@Setter
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "cartId", nullable = false)
    private Cart cart;

    @ManyToOne
    @JoinColumn(name = "productId", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;
}

