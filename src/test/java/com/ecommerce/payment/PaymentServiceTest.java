package com.ecommerce.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
@Transactional
class PaymentServiceTest {
    private static final int INITIAL_STOCK = 100;
    private static final int ORDER_QUANTITY = 10;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentStarter paymentStarter;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentJobRepository paymentJobRepository;

    @MockBean
    private MockPaymentGateway paymentGateway;

    @PersistenceContext
    private EntityManager entityManager;

    private Users user;
    private Product product;

    @BeforeEach
    void setUp() {
        Cart cart = cartRepository.save(new Cart());
        user = userRepository.save(Users.builder()
                                        .subject("payment-test-subject")
                                        .providerId("payment-test-provider")
                                        .provider("test-provider")
                                        .address("test-address")
                                        .email("payment-test@example.com")
                                        .phone("010-1234-1234")
                                        .name("payment-test-user")
                                        .cart(cart)
                                        .build());

        Store store = storeRepository.save(Store.builder()
                                                  .name("payment-test-store")
                                                  .password("password")
                                                  .phoneNumber("010-1234-1234")
                                                  .totalSales(0L)
                                                  .build());

        Category category = new Category();
        category.setName("payment-test-category");
        categoryRepository.save(category);

        product = productRepository.save(Product.builder()
                                                   .name("payment-test-product")
                                                   .price(1_000L)
                                                   .stock(INITIAL_STOCK)
                                                   .description("test-product")
                                                   .store(store)
                                                   .category(category)
                                                   .build());
    }

    @Test
    @DisplayName("결제 실패 이벤트를 처리하면 주문 상태가 PAYMENT_FAILED가 된다")
    void paymentFailureChangesOrderStatus() {
        PaymentJob paymentJob = createPendingPaymentOrder();
        when(paymentGateway.pay()).thenReturn(false);

        paymentStarter.start(paymentJob.getId());
        entityManager.flush();
        entityManager.clear();

        Orders failedOrder = orderRepository.findById(paymentJob.getOrderId()).orElseThrow();
        PaymentJob failedJob = paymentJobRepository.findById(paymentJob.getId()).orElseThrow();
        assertEquals(OrderStatus.PAYMENT_FAILED, failedOrder.getStatus());
        assertEquals(PaymentJobStatus.FAILED, failedJob.getStatus());
    }

    @Test
    @DisplayName("결제 실패 이벤트를 처리하면 주문에서 차감한 재고가 복구된다")
    void paymentFailureRestoresProductStock() {
        PaymentJob paymentJob = createPendingPaymentOrder();
        when(paymentGateway.pay()).thenReturn(false);

        paymentStarter.start(paymentJob.getId());
        entityManager.flush();
        entityManager.clear();

        Product restoredProduct = productRepository.findById(product.getId()).orElseThrow();
        assertEquals(INITIAL_STOCK, restoredProduct.getStock());
    }

    @Test
    @DisplayName("동일한 결제 실패 이벤트를 중복 처리해도 재고는 한 번만 복구된다")
    void duplicatePaymentFailureRestoresProductStockOnlyOnce() {
        PaymentJob paymentJob = createPendingPaymentOrder();
        when(paymentGateway.pay()).thenReturn(false);

        paymentStarter.start(paymentJob.getId());
        paymentStarter.start(paymentJob.getId());
        entityManager.flush();
        entityManager.clear();

        Product restoredProduct = productRepository.findById(product.getId()).orElseThrow();
        Orders failedOrder = orderRepository.findById(paymentJob.getOrderId()).orElseThrow();
        assertEquals(INITIAL_STOCK, restoredProduct.getStock());
        assertEquals(OrderStatus.PAYMENT_FAILED, failedOrder.getStatus());
    }

    @Test
    @DisplayName("결제가 성공하면 PaymentJob을 삭제하지 않고 DONE 상태로 보존한다")
    void successfulPaymentKeepsDonePaymentJob() {
        PaymentJob paymentJob = createPendingPaymentOrder();
        when(paymentGateway.pay()).thenReturn(true);

        paymentStarter.start(paymentJob.getId());
        entityManager.flush();
        entityManager.clear();

        PaymentJob completedJob = paymentJobRepository.findById(paymentJob.getId()).orElseThrow();
        Orders paidOrder = orderRepository.findById(paymentJob.getOrderId()).orElseThrow();
        assertEquals(PaymentJobStatus.DONE, completedJob.getStatus());
        assertEquals(OrderStatus.PAID, paidOrder.getStatus());
    }

    private PaymentJob createPendingPaymentOrder() {
        orderService.orderProductAndPaymentRequest(
                user.getProviderId(),
                OrderProductRequestDto.builder()
                                      .productId(product.getId())
                                      .quantity(ORDER_QUANTITY)
                                      .deliveryAddress("test-delivery-address")
                                      .phoneNumber("010-1234-1234")
                                      .build());
        entityManager.flush();

        return paymentJobRepository.findAll().stream()
                                   .filter(job -> job.getStatus() == PaymentJobStatus.PENDING)
                                   .findFirst()
                                   .orElseThrow();
    }
}
