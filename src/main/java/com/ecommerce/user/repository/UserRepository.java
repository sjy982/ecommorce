package com.ecommerce.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecommerce.cart.model.Cart;
import com.ecommerce.user.model.Users;

public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByProviderId(String providerId);
    @Query("SELECT u.cart FROM Users u WHERE u.providerId = :providerId")
    Optional<Cart> findCartByProviderId(@Param("providerId") String providerId);
}
