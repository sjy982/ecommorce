package com.ecommerce.order.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderProductRequestDto {

    @NotNull(message = "productId is required")
    private long productId;

    @NotNull(message = "productStock is required")
    private int quantity;

    @NotBlank(message = "deliveryAddress is required")
    private String deliveryAddress;

    @NotBlank(message = "phoneNumber is required")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "Phone number must be in the format 010-XXXX-XXXX.")
    private String phoneNumber;
}
