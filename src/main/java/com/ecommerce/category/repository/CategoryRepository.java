package com.ecommerce.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.category.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
}
