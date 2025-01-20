package com.ecommerce.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.user.model.Users;

public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByProviderId(String providerId);
}
