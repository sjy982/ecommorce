package com.ecommerce.user.model;

import com.ecommerce.cart.model.Cart;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, unique = true)
    private String providerId;

    @Column(nullable = false, length = 15)
    private String provider;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 15)
    private String phone;

    @Column
    private String address;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "cartId")
    private Cart cart;

}
