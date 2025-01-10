package com.ecommerce.order.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import com.ecommerce.user.model.User;

@Entity(name = "orders")
@Getter
@Setter
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Column(nullable = false, length = 20)
    private String status = "준비중";

    @Column(nullable = false)
    private String deliveryAddress;

    @Column(nullable = false, length = 15)
    private String phoneNumber;
}

