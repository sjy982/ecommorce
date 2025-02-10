package com.ecommerce.notification.service;

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
import com.ecommerce.order.service.OrderService;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.store.model.Store;
import com.ecommerce.store.repository.StoreRepository;
import com.ecommerce.user.model.Users;
import com.ecommerce.user.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
class NotificationServiceTest {
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

    @Autowired
    private NotificationService notificationService;

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
                         .stock(80)
                         .store(store)
                         .category(category)
                         .build();

        productRepository.save(product);
    }

    @Test
    @DisplayName("주문 후 알림이 생성된다. 알림을 확인한 후 읽지 않은 알림을 조회한다면, isRead가 false 알림만이 조회되어야 한다. 또한 내림차순 정렬되어 있어야 한다.")
    @Transactional
    void givenOrderProductAfterCreatedNotification_whenFindByStoreIdAndIsReadFalseOrderByCreatedAtDesc_thenSelectIsReadFalseOrderByCreatedAtDescNotification() {
        // Given
        OrderProductRequestDto requestDto1 = OrderProductRequestDto.builder()
                                                                  .productId(product.getId())
                                                                  .quantity(10)
                                                                  .deliveryAddress("test Address")
                                                                  .phoneNumber("010-1234-1234").build();

        OrderProductRequestDto requestDto2 = OrderProductRequestDto.builder()
                                                                  .productId(product.getId())
                                                                  .quantity(20)
                                                                  .deliveryAddress("test Address")
                                                                  .phoneNumber("010-1234-1234").build();

        OrderProductRequestDto requestDto3 = OrderProductRequestDto.builder()
                                                                   .productId(product.getId())
                                                                   .quantity(30)
                                                                   .deliveryAddress("test Address")
                                                                   .phoneNumber("010-1234-1234").build();

        // When && Then
        orderService.orderProduct(user.getProviderId(), requestDto1);
        orderService.orderProduct(user.getProviderId(), requestDto2);
        orderService.orderProduct(user.getProviderId(), requestDto3);

        List<Notification> allNotifications = notificationService.getAllNotifications(product.getStore().getId());
        assertEquals(allNotifications.get(0).getOrder().getOrderDate().isAfter(allNotifications.get(1).getOrder().getOrderDate()), true);

        notificationService.markAsRead(allNotifications.get(2).getId());

        List<Notification> isReadFalseNotifications = notificationRepository.findByStoreIdAndIsReadFalseOrderByCreatedAtDesc(product.getStore().getId());
        assertEquals(isReadFalseNotifications.size(), 2);
        assertEquals(isReadFalseNotifications.get(0).getIsRead(), false);
        assertEquals(isReadFalseNotifications.get(1).getIsRead(), false);
        assertEquals(isReadFalseNotifications.get(0).getStore().getId(), product.getStore().getId());
        assertEquals(isReadFalseNotifications.get(1).getStore().getId(), product.getStore().getId());

        List<Orders> newOrder = orderRepository.findAllByUserProviderId(user.getProviderId());
        assertEquals(isReadFalseNotifications.get(0).getOrder().getId(), newOrder.get(2).getId());
        assertEquals(isReadFalseNotifications.get(1).getOrder().getId(), newOrder.get(1).getId());

        assertEquals(isReadFalseNotifications.get(0).getOrder().getOrderDate().isAfter(isReadFalseNotifications.get(1).getOrder().getOrderDate()), true);
    }
}
