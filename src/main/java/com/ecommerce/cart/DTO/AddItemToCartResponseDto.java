package com.ecommerce.cart.DTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddItemToCartResponseDto {
    private String productName;
    private Integer quantity;
}
