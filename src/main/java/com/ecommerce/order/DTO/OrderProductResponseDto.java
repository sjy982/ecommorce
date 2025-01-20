package com.ecommerce.order.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class OrderProductResponseDto {
    private String productName;
    private long productPrice;
    private int quantity;
    private String deliveryAddress;
    private String phoneNumber;
}
