package com.ecommerce.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
import com.ecommerce.order.DTO.OrderProductRequestDto;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.model.Orders;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.order.service.OrderService;
import com.ecommerce.payment.job.model.PaymentJob;
import com.ecommerce.payment.job.model.PaymentJobStatus;
import com.ecommerce.payment.job.repository.PaymentJobRepository;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.store.model.Store;
import com.ecommerce.store.repository.StoreRepository;
import com.ecommerce.user.model.Users;
import com.ecommerce.user.repository.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest
@ActiveProfiles("test")
class PaymentServiceTest {

    @Autowired private PaymentService paymentService;
    @Autowired private OrderService orderService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private PaymentJobRepository paymentJobRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private StoreRepository storeRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private CartRepository cartRepository;

    @PersistenceContext private EntityManager entityManager;

    private Users user;
    private Product product;
    private Store store;

    private static final int INITIAL_STOCK = 100;
    private static final int ORDER_QUANTITY = 10;

    @BeforeEach
    void setup() {
        Cart cart = new Cart();
        cartRepository.save(cart);

        user = Users.builder()
                    .subject("paymentTestSubject")
                    .providerId("paymentTestProviderId")
                    .provider("testProvider")
                    .address("testAddress")
                    .email("payment-test@email.com")
                    .phone("010-9999-9999")
                    .name("paymentTestUser")
                    .cart(cart)
                    .build();
        userRepository.save(user);

        store = Store.builder()
                     .name("paymentTestStore")
                     .password("testPw")
                     .phoneNumber("010-9999-9999")
                     .totalSales(0L)
                     .build();
        storeRepository.save(store);

        Category category = new Category();
        category.setName("paymentTestCategory");
        categoryRepository.save(category);

        product = Product.builder()
                         .name("paymentTestProduct")
                         .price(1000L)
                         .stock(INITIAL_STOCK)
                         .store(store)
                         .category(category)
                         .build();
        productRepository.save(product);
    }

    @Test
    @Transactional
    @DisplayName("결제 실패 시 주문 상태가 PAYMENT_FAILED로 변경되어야 한다.")
    void givenPendingOrder_whenMarkPaidFailed_thenOrderStatusIsPaymentFailed() {
        // Given
        OrderProductRequestDto dto = OrderProductRequestDto.builder()
                .productId(product.getId())
                .quantity(ORDER_QUANTITY)
                .deliveryAddress("test address")
                .phoneNumber("010-9999-9999")
                .build();
        orderService.orderProductAndPaymentRequest(user.getProviderId(), dto);

        entityManager.flush();
        entityManager.clear();

        Orders order = orderRepository.findAllByUserProviderIdOrderByOrderDateDesc(user.getProviderId()).get(0);
        PaymentJob paymentJob = paymentJobRepository.findAll().stream()
                .filter(j -> order.getId().equals(j.getOrderId()))
                .findFirst().orElseThrow();

        // When
        paymentService.markPaidFailed(paymentJob.getId());

        entityManager.flush();
        entityManager.clear();

        // Then
        Orders updatedOrder = orderRepository.findById(order.getId()).orElseThrow();
        assertEquals(OrderStatus.PAYMENT_FAILED, updatedOrder.getStatus());
    }

    @Test
    @Transactional
    @DisplayName("결제 실패 시 차감된 재고가 원래대로 복구되어야 한다.")
    void givenPendingOrder_whenMarkPaidFailed_thenStockIsRestored() {
        // Given
        OrderProductRequestDto dto = OrderProductRequestDto.builder()
                .productId(product.getId())
                .quantity(ORDER_QUANTITY)
                .deliveryAddress("test address")
                .phoneNumber("010-9999-9999")
                .build();
        orderService.orderProductAndPaymentRequest(user.getProviderId(), dto);

        entityManager.flush();
        entityManager.clear();

        Orders order = orderRepository.findAllByUserProviderIdOrderByOrderDateDesc(user.getProviderId()).get(0);
        PaymentJob paymentJob = paymentJobRepository.findAll().stream()
                .filter(j -> order.getId().equals(j.getOrderId()))
                .findFirst().orElseThrow();

        // Verify stock was decreased before compensation
        Product stockAfterOrder = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(INITIAL_STOCK - ORDER_QUANTITY, stockAfterOrder.getStock());

        // When
        paymentService.markPaidFailed(paymentJob.getId());

        entityManager.flush();
        entityManager.clear();

        // Then
        Product restoredProduct = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(INITIAL_STOCK, restoredProduct.getStock());
    }

    @Test
    @Transactional
    @DisplayName("결제 실패 처리가 중복 호출되어도 재고는 한 번만 복구되어야 한다.")
    void givenPendingOrder_whenMarkPaidFailedCalledTwice_thenStockRestoredOnce() {
        // Given
        OrderProductRequestDto dto = OrderProductRequestDto.builder()
                .productId(product.getId())
                .quantity(ORDER_QUANTITY)
                .deliveryAddress("test address")
                .phoneNumber("010-9999-9999")
                .build();
        orderService.orderProductAndPaymentRequest(user.getProviderId(), dto);

        entityManager.flush();
        entityManager.clear();

        Orders order = orderRepository.findAllByUserProviderIdOrderByOrderDateDesc(user.getProviderId()).get(0);
        PaymentJob paymentJob = paymentJobRepository.findAll().stream()
                .filter(j -> order.getId().equals(j.getOrderId()))
                .findFirst().orElseThrow();

        // When: 동일한 결제 실패를 두 번 처리
        paymentService.markPaidFailed(paymentJob.getId());
        entityManager.flush();
        entityManager.clear();

        paymentService.markPaidFailed(paymentJob.getId()); // 중복 호출
        entityManager.flush();
        entityManager.clear();

        // Then: 재고는 INITIAL_STOCK 그대로여야 하며, ORDER_QUANTITY 만큼 초과 복구되면 안 된다
        Product restoredProduct = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(INITIAL_STOCK, restoredProduct.getStock());
    }

    @Test
    @Transactional
    @DisplayName("결제 실패 후 PaymentJob 상태가 FAILED로 보존되어야 한다.")
    void givenPendingOrder_whenMarkPaidFailed_thenPaymentJobStatusIsFailed() {
        // Given
        OrderProductRequestDto dto = OrderProductRequestDto.builder()
                .productId(product.getId())
                .quantity(ORDER_QUANTITY)
                .deliveryAddress("test address")
                .phoneNumber("010-9999-9999")
                .build();
        orderService.orderProductAndPaymentRequest(user.getProviderId(), dto);

        entityManager.flush();
        entityManager.clear();

        Orders order = orderRepository.findAllByUserProviderIdOrderByOrderDateDesc(user.getProviderId()).get(0);
        PaymentJob paymentJob = paymentJobRepository.findAll().stream()
                .filter(j -> order.getId().equals(j.getOrderId()))
                .findFirst().orElseThrow();

        // When
        paymentService.markPaidFailed(paymentJob.getId());

        entityManager.flush();
        entityManager.clear();

        // Then
        PaymentJob updatedJob = paymentJobRepository.findById(paymentJob.getId()).orElseThrow();
        assertEquals(PaymentJobStatus.FAILED, updatedJob.getStatus());
    }
}
