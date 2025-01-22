package com.ecommerce.cart.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItemResponseDto {
    private Long cartItemId;
    private Long productId;
    private String productName;
    private Long productPrice;
    private Integer productQuantity;
}
