package com.ecommerce.orderItem.model;

import com.ecommerce.order.model.Orders;
import com.ecommerce.product.model.Product;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "order_item")
@Getter
@Setter
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "orderId", nullable = false)
    private Orders order;

    @ManyToOne
    @JoinColumn(name = "productId", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int quantity;
}

