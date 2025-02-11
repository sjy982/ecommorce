package com.ecommerce.order.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.notification.service.NotificationService;
import com.ecommerce.order.DTO.OrderProductDto;
import com.ecommerce.order.DTO.OrderProductRequestDto;
import com.ecommerce.order.DTO.OrderProductResponseDto;
import com.ecommerce.order.model.Orders;
import com.ecommerce.order.repository.OrderRepository;
import com.ecommerce.product.model.Product;


import com.ecommerce.product.service.ProductService;
import com.ecommerce.store.model.Store;

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

    @Transactional
    public OrderProductResponseDto orderProduct(String userId, OrderProductRequestDto dto) {
        Product product = productService.decreaseStock(dto.getProductId(), dto.getQuantity());

        Store store = product.getStore();
        long totalPrice = product.getPrice() * dto.getQuantity();

        store.setTotalSales(store.getTotalSales() + totalPrice);

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

//    public OrderProductResponseDto getUserOrderProductResponseDto(Long orderId, String providerId) {
//        Orders order = findByIdAndProviderId(orderId, providerId);
//        return convertOrdersToOrderProductResponse(order);
//    }
//
//    public OrderProductResponseDto getStoreOrderProductResponseDto(Long orderId, Long storeId) {
//        Orders order = findByIdAndStoreId(orderId, storeId);
//        return convertOrdersToOrderProductResponse(order);
//    }

    private static OrderProductResponseDto convertOrdersToOrderProductResponse(Orders order) {
        return OrderProductResponseDto.builder()
                                      .orderProduct(OrderProductDto.builder()
                                                            .name(order.getProduct().getName())
                                                            .price(order.getProduct().getPrice())
                                                            .quantity(order.getQuantity()).build())
                                      .deliveryAddress(order.getDeliveryAddress())
                               .phoneNumber(order.getPhoneNumber()).build();
    }

//    private  Orders findByIdAndProviderId(Long orderId, String providerId) {
//        Orders order = orderRepository.findByIdAndProviderId(orderId, providerId).orElseThrow(() -> new UsernameNotFoundException("order not found"));
//        return order;
//    }
//
//    private  Orders findByIdAndStoreId(Long orderId, Long storeId) {
//        Orders order = orderRepository.findByIdAndStoreId(orderId, storeId).orElseThrow(() -> new UsernameNotFoundException("order not found"));
//        return order;
//    }
}
