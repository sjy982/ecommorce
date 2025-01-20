package com.ecommerce.cart.service;

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

import com.ecommerce.cart.DTO.AddItemToCartRequestDto;
import com.ecommerce.cart.DTO.AddItemToCartResponseDto;
import com.ecommerce.cart.model.Cart;
import com.ecommerce.cart.repository.CartRepository;
import com.ecommerce.cartItem.model.CartItem;
import com.ecommerce.cartItem.repository.CartItemRepository;
import com.ecommerce.category.model.Category;
import com.ecommerce.category.repository.CategoryRepository;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;

import com.ecommerce.store.model.Store;
import com.ecommerce.store.repository.StoreRepository;
import com.ecommerce.user.model.Users;
import com.ecommerce.user.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
class CartServiceTest {
    @Autowired
    private CartService cartService;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    private Users user;

    private Product product;


    @BeforeEach
    void setup() {
        Cart cart = new Cart();
        cartRepository.save(cart);

        user = Users.builder()
                    .subject("testSubject")
                    .providerId("testProviderId")
                    .provider("testProvider")
                    .address("testAddress")
                    .email("testemail")
                    .phone("010-1234-1234")
                    .name("testName")
                    .cart(cart)
                    .build();

        userRepository.save(user);

        Store store = Store.builder()
                     .name("testName")
                     .password("testPw")
                     .phoneNumber("010-1234-1234")
                     .totalSales(1000L)
                     .build();

        storeRepository.save(store);

        Category category = new Category();
        category.setName("testName");

        categoryRepository.save(category);

        product = Product.builder()
                         .name("testName")
                         .price(100L)
                         .stock(50)
                         .store(store)
                         .category(category)
                         .build();

        productRepository.save(product);
    }

    @Test
    @DisplayName("유저가 상품을 카트에 담을 때 재고가 충분하다면 담을 수 있어야 한다.")
    @Transactional
    void givenAddItemToCartRequestDto_whenAddItemToCart_thenSuccessAdd() {
        // Given
        AddItemToCartRequestDto requestDto = AddItemToCartRequestDto.builder()
                .productId(1L)
                .quantity(10).build();

        // When
        AddItemToCartResponseDto responseDto = cartService.addItemToCart(user.getProviderId(), requestDto);

        // Then
        assertEquals(product.getName(), responseDto.getProductName());

        CartItem newCartItem = cartItemRepository.findByCartId(user.getCart().getId()).get();

        assertEquals(newCartItem.getProduct().getName(), responseDto.getProductName());
        assertEquals(newCartItem.getQuantity(), responseDto.getQuantity());
    }

    @Test
    @DisplayName("유저가 상품을 카트에 담을 때 재고가 충분하지 않다면 담지 못해야 한다.")
    @Transactional
    void givenOutOfStockAddItemToCartRequestDto_whenAddItemToCart_thenFailedAdd() {
        // Given
        AddItemToCartRequestDto requestDto = AddItemToCartRequestDto.builder()
                                                                    .productId(1L)
                                                                    .quantity(60).build();

        // When && Then
        assertThrows(UsernameNotFoundException.class, () -> cartService.addItemToCart(user.getProviderId(), requestDto));
    }
}
