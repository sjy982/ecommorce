package com.ecommerce.order.service;

import java.util.List;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.notification.service.NotificationService;
import com.ecommerce.order.DTO.OrderProductDto;
import com.ecommerce.order.DTO.OrderProductRequestDto;
import com.ecommerce.order.DTO.OrderProductResponseDto;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.model.Orders;
import com.ecommerce.order.repository.OrderRepository;

import com.ecommerce.payment.MockPaymentGateway;
import com.ecommerce.payment.exception.PaymentFailedException;
import com.ecommerce.product.model.Product;


import com.ecommerce.product.service.ProductService;
import com.ecommerce.store.model.Store;

import com.ecommerce.store.service.StoreService;
import com.ecommerce.user.model.Users;

import com.ecommerce.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final StoreService storeService;
    private final MockPaymentGateway paymentGateway;

    @Transactional
    public OrderProductResponseDto orderProduct(String userId, OrderProductRequestDto dto) {
        Product product = productService.findById(dto.getProductId());
        productService.decreaseStock(product.getId(), dto.getQuantity()); //수량 감소

        Store store = product.getStore();
        storeService.increaseTotalSales(store.getId(), product.getPrice() * dto.getQuantity()); //총 금액 증가

        Users user = userService.findByProviderId(userId);
        Orders order = Orders.builder()
                             .user(user)
                             .store(store)
                             .product(product)
                             .quantity(dto.getQuantity())
                             .deliveryAddress(dto.getDeliveryAddress())
                             .phoneNumber(dto.getPhoneNumber())
                             .build();

        orderRepository.save(order);
        notificationService.createNotification(order);

        return convertOrdersToOrderProductResponse(order);
    }

    @Transactional
    public OrderProductResponseDto orderProduct2(String providerId, OrderProductRequestDto dto) {
        //재고 차감
        Product product = productService.findById(dto.getProductId());
        product.decreaseStock(dto.getQuantity());

        //모의 결제
        final boolean paymentResult = paymentGateway.pay();
        if (!paymentResult) {
            //결제 실패
            throw new PaymentFailedException("payment failed");
        }

        //결제 성공
        //매출 올리고
        Store store = product.getStore();
        store.increaseTotalSales(product.getPrice() * dto.getQuantity());

        //주문 데이터 생성
        Orders order = Orders.builder()
                             .user(userService.findByProviderId(providerId))
                             .store(store)
                             .product(product)
                             .quantity(dto.getQuantity())
                             .deliveryAddress(dto.getDeliveryAddress())
                             .phoneNumber(dto.getPhoneNumber())
                             .status(OrderStatus.PAID)
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
