package com.ecommerce.cart.DTO;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemsOrderRequestDto {
    @NotEmpty(message = "CartItemId list cannot be empty")
    private List<@NotNull(message = "CartItemId cannot be null") Long> cartItemIds;

    @NotBlank(message = "deliveryAddress is required")
    private String deliveryAddress;

    @NotBlank(message = "phoneNumber is required")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "Phone number must be in the format 010-XXXX-XXXX.")
    private String phoneNumber;


}
