package com.ecommerce.product.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.category.model.Category;
import com.ecommerce.category.repository.CategoryRepository;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.service.ProductService;
import com.ecommerce.store.model.Store;
import com.ecommerce.store.repository.StoreRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ProductRepositoryTest {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductService productService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Product product;

    @BeforeEach
    void setup() {
        Store store = Store.builder()
                           .name("testName")
                           .password("testPw")
                           .phoneNumber("010-1234-1234")
                           .totalSales(0L)
                           .build();

        storeRepository.save(store);

        Category category = new Category();
        category.setName("testName");

        categoryRepository.save(category);

        product = Product.builder()
                         .name("testName")
                         .price(100)
                         .stock(10)
                         .store(store)
                         .category(category)
                         .build();

        productRepository.save(product);
    }

    @Test
    @DisplayName("재고량이 충분할 때는 업데이트가 되어야 한다.")
    void givenSufficientStock_whenDecreaseStock_thenUpdateSuccess() {
        // Given
        long productId = product.getId();
        long curStock = product.getStock();
        int quantity = 5;

        // When
        productService.decreaseStock(productId, quantity);
        entityManager.flush();
        entityManager.clear();

        // Then
        Product updatedProduct = productService.findById(productId);
        assertEquals(curStock - quantity, updatedProduct.getStock());

    }

    @Test
    @DisplayName("재고량이 충분하지 않을 때는 업데이트 되지 않아야 한다.")
    void givenInSufficientStock_whenDecreaseStock_thenUpdateFailed() {
        // Given
        long productId = product.getId();
        int quantity = 15;

        // When && Then
        assertThrows(UsernameNotFoundException.class, () -> {
            productService.decreaseStock(productId, quantity);
        });
    }
}
