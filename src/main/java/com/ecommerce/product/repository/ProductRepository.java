package com.ecommerce.product.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.product.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsById(long productId);
    Optional<Product> findById(long id);
}
