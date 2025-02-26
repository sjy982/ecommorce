package com.ecommerce.order.service;

import java.util.List;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.notification.service.NotificationService;
import com.ecommerce.order.DTO.OrderProductDto;
import com.ecommerce.order.DTO.OrderProductRequestDto;
import com.ecommerce.order.DTO.OrderProductResponseDto;
import com.ecommerce.order.model.Orders;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.product.dto.PriceStoreIdDto;
import com.ecommerce.product.model.Product;

import com.ecommerce.product.projection.PriceStoreIdProjection;
import com.ecommerce.product.service.ProductService;
import com.ecommerce.store.model.Store;

import com.ecommerce.store.service.StoreService;
import com.ecommerce.user.model.Users;

import com.ecommerce.user.service.UserService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final StoreService storeService;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public OrderProductResponseDto orderProduct(String providerId, OrderProductRequestDto dto) {
        PriceStoreIdDto priceStoreIdDto = productService.findPriceAndStoreIdByProductId(dto.getProductId());

        Product productRef = entityManager.getReference(Product.class, dto.getProductId());
        productService.decreaseStock(productRef.getId(), dto.getQuantity());

        Store storeRef = entityManager.getReference(Store.class, priceStoreIdDto.getStoreId());
        storeService.increaseTotalSales(storeRef.getId(), priceStoreIdDto.getPrice() * dto.getQuantity()); //총 금액 증가

        Users userRef = entityManager.getReference(Users.class, userService.findIdByProviderId(providerId));

        Orders order = Orders.builder()
                .user(userRef)
                .store(storeRef)
                .product(productRef)
                .quantity(dto.getQuantity())
                .deliveryAddress(dto.getDeliveryAddress())
                .phoneNumber(dto.getPhoneNumber())
                             .build();

        orderRepository.save(order);
        notificationService.createNotification(order);

        return convertOrdersToOrderProductResponse(order);
    }

    public List<OrderProductResponseDto> getUserAllOrderProductsResponseDto(String providerId) {
        List<Orders> orders = orderRepository.findAllByUserProviderIdOrderByOrderDateDesc(providerId);
        return orders.stream()
                .map((order) -> convertOrdersToOrderProductResponse(order)).toList();
    }

    public List<OrderProductResponseDto> getStoreAllOrderProductsResponseDto(Long storeId) {
        List<Orders> orders = orderRepository.findAllByStoreIdOrderByOrderDateDesc(storeId);
        return orders.stream()
                .map((order) -> convertOrdersToOrderProductResponse(order)).toList();
    }

    public OrderProductResponseDto getUserOrderProductResponseDto(Long orderId, String providerId) {
        Orders order = findByIdAndProviderId(orderId, providerId);
        return convertOrdersToOrderProductResponse(order);
    }

    public OrderProductResponseDto getStoreOrderProductResponseDto(Long orderId, Long storeId) {
        Orders order = findByIdAndStoreId(orderId, storeId);
        return convertOrdersToOrderProductResponse(order);
    }

    private static OrderProductResponseDto convertOrdersToOrderProductResponse(Orders order) {
        return OrderProductResponseDto.builder()
                                      .orderProduct(OrderProductDto.builder()
                                                            .name(order.getProduct().getName())
                                                            .price(order.getProduct().getPrice())
                                                            .quantity(order.getQuantity()).build())
                                      .deliveryAddress(order.getDeliveryAddress())
                               .phoneNumber(order.getPhoneNumber()).build();
    }

    private  Orders findByIdAndProviderId(Long orderId, String providerId) {
        Orders order = orderRepository.findByIdAndProviderId(orderId, providerId).orElseThrow(() -> new UsernameNotFoundException("order not found"));
        return order;
    }

    private  Orders findByIdAndStoreId(Long orderId, Long storeId) {
        Orders order = orderRepository.findByIdAndStoreId(orderId, storeId).orElseThrow(() -> new UsernameNotFoundException("order not found"));
        return order;
    }
}
