package com.ecommerce.order.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class OrderProductResponseDto {
    private OrderProductDto orderProduct;
    private String deliveryAddress;
    private String phoneNumber;
}
