package com.ecommerce.order.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

import com.ecommerce.product.model.Product;
import com.ecommerce.store.model.Store;
import com.ecommerce.user.model.Users;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private Users user;

    @ManyToOne
    @JoinColumn(name = "storeId", nullable = false)
    private Store store;

    @ManyToOne
    @JoinColumn(name = "productId", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private String deliveryAddress;

    @Column(nullable = false, length = 15)
    private String phoneNumber;

    @PrePersist
    public void prePersist() {
        orderDate = (orderDate == null) ? LocalDateTime.now() : orderDate;
        status = (status == null) ? "준비중" : status;
    }
}
