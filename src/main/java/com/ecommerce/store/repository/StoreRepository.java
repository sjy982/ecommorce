package com.ecommerce.store.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.store.model.Store;

public interface StoreRepository extends JpaRepository<Store, Integer> {
    Optional<Store> findByName(String name);

    boolean existsByName(String name);
}
