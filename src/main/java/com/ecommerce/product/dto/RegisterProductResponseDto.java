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
    private Long price;
    private Integer stock;
    private String description;
    private String category;
}
