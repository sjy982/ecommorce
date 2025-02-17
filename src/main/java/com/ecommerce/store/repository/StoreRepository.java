package com.ecommerce.store.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecommerce.store.model.Store;

public interface StoreRepository extends JpaRepository<Store, Long> {
    Optional<Store> findByName(String name);
    boolean existsByName(String name);

    boolean existsById(long storeId);

    @Modifying
    @Query("UPDATE Store s SET s.totalSales = s.totalSales + :amount WHERE s.id = :storeId")
    int increaseTotalSales(@Param("storeId") Long storeId, @Param("amount") long amount);
}
