package com.ecommerce.product.model;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.ecommerce.category.model.Category;
import com.ecommerce.store.model.Store;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private Integer stock;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "storeId", nullable = false)
    private Store store;

    @ManyToOne
    @JoinColumn(name = "categoryId", nullable = false)
    private Category category;

    public void decreaseStock(int quantity) {
        if (!checkStock(quantity)) {
            throw new UsernameNotFoundException("재고가 부족합니다.");
        }
        stock -= quantity;
    }

    public void increaseStock(int quantity) {
        stock += quantity;
    }

    public boolean checkStock(int quantity) {
        return stock >= quantity;
    }
}

