package com.ecommerce.order.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecommerce.order.model.Orders;

public interface OrderRepository extends JpaRepository<Orders, Long> {
    List<Orders> findAllByUserProviderId(String providerId);

    @Query("SELECT o FROM Orders o JOIN o.user u WHERE u.providerId = :providerId AND o.id = :orderId")
    Optional<Orders> findByIdAndProviderId(@Param("orderId") Long orderId, @Param("providerId") String providerId);

    @Query("SELECT o FROM Orders o JOIN o.store s WHERE s.id = :storeId AND o.id = :orderId")
    Optional<Orders> findByIdAndStoreId(@Param("orderId") Long orderId, @Param("storeId") Long storeId);

}
