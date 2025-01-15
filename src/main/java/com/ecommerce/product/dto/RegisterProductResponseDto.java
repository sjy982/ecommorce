package com.ecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class RegisterProductResponseDto {
    private String store;
    private String name;
    private long price;
    private long stock;
    private String description;
    private String category;
}
