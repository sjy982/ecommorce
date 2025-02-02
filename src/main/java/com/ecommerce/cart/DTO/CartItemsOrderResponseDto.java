package com.ecommerce.cart.DTO;

import java.util.List;

import com.ecommerce.order.DTO.OrderProductDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItemsOrderResponseDto {
    private List<OrderProductDto> orderProducts;
    private String deliveryAddress;
    private String phoneNumber;
}
