package com.ecommerce.product.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.category.model.Category;
import com.ecommerce.category.repository.CategoryRepository;
import com.ecommerce.product.dto.RegisterProductRequestDto;
import com.ecommerce.product.dto.RegisterProductResponseDto;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.store.model.Store;
import com.ecommerce.store.repository.StoreRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @InjectMocks
    private  ProductService productService;

    @Test
    @DisplayName("storeName과 RegisterProductRequestDto가 주어졌을 때 생성과 동시에 RegisterProductResponseDto를 반환해야 한다.")
    void givenStoreNameAndRegisterProductRequestDto_whenRegisterProduct_thenShouldCreatedProductAndReturnREgisterProductResponseDto() {
        // Given
        Store store = new Store();
        store.setName("testStoreName");

        Category category = new Category();
        category.setName("testCategoryName");

        RegisterProductRequestDto requestDto = RegisterProductRequestDto.builder()
                                                                        .categoryId(1L)
                                                                        .stock(10)
                                                                        .price(100)
                                                                        .name("testName")
                                                                        .description("testDiscription")
                                                                        .build();

        when(storeRepository.findByName(store.getName())).thenReturn(Optional.of(store));
        when(categoryRepository.findById(requestDto.getCategoryId())).thenReturn(Optional.of(category));

        // When
        RegisterProductResponseDto responseDto = productService.registerProduct(store.getName(), requestDto);

        // Then
        assertEquals(10, responseDto.getStock());
        assertEquals(100, responseDto.getPrice());
        assertEquals("testName", responseDto.getName());
        assertEquals("testDiscription", responseDto.getDescription());
        assertEquals("testStoreName", responseDto.getStore());
        assertEquals("testCategoryName", responseDto.getCategory());
        verify(productRepository, times(1)).save(any(Product.class));

    }
}
