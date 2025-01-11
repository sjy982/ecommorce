package com.ecommerce.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.user.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByProviderId(String providerId);
}
