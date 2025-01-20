package com.ecommerce.order.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        OrderProductResponseDto responseDto = OrderProductResponseDto.builder()
                .productName(order.getProduct().getName())
                .productPrice(order.getProduct().getPrice())
                .quantity(order.getQuantity())
                .deliveryAddress(order.getDeliveryAddress())
                .phoneNumber(order.getPhoneNumber()).build();

        return responseDto;
    }
}
