package com.ecommerce.cart.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddItemToCartRequestDto {
    @NotNull(message = "productId is required")
    private Long productId;

    @NotNull(message = "quantity is required")
    private Integer quantity;
}
