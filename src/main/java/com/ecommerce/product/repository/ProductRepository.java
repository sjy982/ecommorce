package com.ecommerce.product.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecommerce.product.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsById(long productId);
    Optional<Product> findById(long id);

    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock - :quantity WHERE p.id = :productId AND p.stock >= :quantity")
    int decreaseStock(@Param("productId") Long productId, @Param("quantity") int quantity);
}
