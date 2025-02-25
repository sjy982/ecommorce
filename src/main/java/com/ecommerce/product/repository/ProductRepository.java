package com.ecommerce.product.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecommerce.product.model.Product;
import com.ecommerce.product.projection.PriceStoreIdProjection;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsById(long productId);
    Optional<Product> findById(long id);

    @Query("SELECT p.price as price, p.store.id as storeId " +
           "FROM Product p " +
           "WHERE p.id = :productId")
    Optional<PriceStoreIdProjection> findPriceAndStoreIdByProductId(@Param("productId") Long productId);

    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock - :quantity WHERE p.id = :productId AND p.stock >= :quantity")
    int decreaseStock(@Param("productId") Long productId, @Param("quantity") int quantity);
}
