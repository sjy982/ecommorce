package com.ecommerce.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.cart.model.Cart;
import com.ecommerce.cart.repository.CartRepository;
import com.ecommerce.category.model.Category;
import com.ecommerce.category.repository.CategoryRepository;
import com.ecommerce.notification.model.Notification;
import com.ecommerce.notification.repository.NotificationRepository;
import com.ecommerce.order.DTO.OrderProductRequestDto;
import com.ecommerce.order.DTO.OrderProductResponseDto;
import com.ecommerce.order.model.Orders;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.store.model.Store;
import com.ecommerce.store.repository.StoreRepository;
import com.ecommerce.user.model.Users;
import com.ecommerce.user.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
class OrderServiceTest {
    @Autowired
    private OrderService orderService;

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
    private NotificationRepository notificationRepository;

    private Users user;
    private Product product;

    private Store store;

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

        store = Store.builder()
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
                         .stock(20)
                         .store(store)
                         .category(category)
                         .build();

        productRepository.save(product);
    }

    @Test
    @DisplayName("유저가 특정 상품을 주문했을 때 해당 상품의 재고가 충분한다면 주민이 완료되어야 한다.")
    @Transactional
    void givenOrderProductRequestDto_whenOrderProduct_thenSuccessOrder() {
        // Given
        OrderProductRequestDto requestDto = OrderProductRequestDto.builder()
                .productId(product.getId())
                .quantity(10)
                .deliveryAddress("test Address")
                .phoneNumber("010-1234-1234").build();

        // When
        OrderProductResponseDto responseDto = orderService.orderProduct(user.getProviderId(), requestDto);

        // Then
        assertEquals(product.getName(), responseDto.getOrderProduct().getName());
        assertEquals(product.getPrice(), responseDto.getOrderProduct().getPrice());
        assertEquals(requestDto.getQuantity(), responseDto.getOrderProduct().getQuantity());
        assertEquals(requestDto.getDeliveryAddress(), responseDto.getDeliveryAddress());
        assertEquals(requestDto.getPhoneNumber(), responseDto.getPhoneNumber());

        List<Orders> newOrder = orderRepository.findAllByUserProviderId(user.getProviderId());
        Product updatedProduct = productRepository.findById(product.getId()).get();
        Store updatedStore = storeRepository.findById(store.getId()).get();
        List<Notification> newNotifications = notificationRepository.findByStoreIdOrderByCreatedAtDesc(store.getId());

        assertEquals(newOrder.get(0).getProduct().getId(), updatedProduct.getId());
        assertEquals(newOrder.get(0).getStore().getId(), updatedStore.getId());
        assertEquals(newOrder.get(0).getUser().getProviderId(), user.getProviderId());
        assertEquals(newNotifications.get(0).getStore().getId(), updatedStore.getId());
        assertEquals(newNotifications.get(0).getOrder().getId(), newOrder.get(0).getId());

        // 업데이트 된 stock, totalSales 검사
        assertEquals(10, updatedProduct.getStock());
        assertEquals(2000, updatedStore.getTotalSales());
    }
}
