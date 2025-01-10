package com.ecommerce.cart.model;

import com.ecommerce.user.model.User;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "cart")
@Getter
@Setter
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
}
